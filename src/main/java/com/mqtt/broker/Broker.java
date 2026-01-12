package com.mqtt.broker;

import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.connection.ClientConnection;
import com.mqtt.broker.connection.ServerListener;
import com.mqtt.broker.decoder.MqttPacketDecoder;
import com.mqtt.broker.event.BrokerEventListener;
import com.mqtt.broker.event.BrokerEventPublisher;
import com.mqtt.broker.event.ConnectionLostEvent;
import com.mqtt.broker.handler.MqttPacketHandler;
import com.mqtt.broker.packet.MqttPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Optional.ofNullable;

@Slf4j
public class Broker implements AutoCloseable {
    private final AtomicBoolean running = new AtomicBoolean(true);

    private final BrokerConfiguration config;
    private final BrokerContext context;
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final MqttPacketDecoder packetDecoder;
    private final MqttPacketHandler packetHandler;
    private final Map<SocketChannel, ClientConnection> connections;
    private final BrokerEventPublisher eventPublisher;
    private final ServerListener serverListener;
    private final ExecutorService packetExecutor;

    public Broker(BrokerConfiguration config, BrokerContext context) throws IOException {
        this.config = config;
        this.context = context;
        this.selector = Selector.open();
        this.connections = new ConcurrentHashMap<>();
        this.serverListener = new ServerListener(selector, config, connections);
        this.serverChannel = serverListener.setup();
        this.packetDecoder = new MqttPacketDecoder();
        this.packetHandler = new MqttPacketHandler(context);
        this.eventPublisher = new BrokerEventPublisher();
        eventPublisher.addListener(new BrokerEventListener(context));
        this.packetExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void start() {
        log.info("Broker started on {}:{}", config.getServer().getHost(), config.getServer().getPort());
        try {
            while (running.get()) {
                selector.select(config.getMqtt().getKeepAliveCheckIntervalMs());
                if (!selector.isOpen()) {
                    break;
                }
                checkKeepAliveTimeouts();
                var keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    var key = keyIterator.next();
                    try {
                        if (key.isAcceptable()) {
                            serverListener.accept(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (Exception e) {
                        log.error("Error handling client {}: {}", key, e.getMessage());
                        cleanupClient(key);
                    } finally {
                        keyIterator.remove();
                    }
                }
            }
        } catch (IOException e) {
            log.error("Broker encountered an error: {}", e.getMessage());
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        var clientChannel = (SocketChannel) key.channel();
        var connection = connections.get(clientChannel);
        if (connection == null) {
            return;
        }
        int bytesRead = connection.read();
        if (bytesRead == -1) {
            log.info("Connection closed by: {}", connection.getRemoteAddress());
            cleanupClient(key);
            return;
        }
        var buffer = connection.getBuffer();
        buffer.flip(); // flip the buffer for reading
        while (buffer.hasRemaining()) {
            var optionalPacket = packetDecoder.decode(buffer);
            if (optionalPacket == null) {
                buffer.reset(); // incomplete packet, wait for more data
                break;
            }
            packetExecutor.submit(() -> processPacket(clientChannel, optionalPacket));
        }
        buffer.compact(); // compact the buffer to preserve incomplete data
    }

    private void processPacket(SocketChannel clientChannel, MqttPacket packet) {
        try {
            updateClientActivity(clientChannel);
            var handlerResult = packetHandler.handle(clientChannel, packet);
            handlerResult.responsePacket()
                    .ifPresent(responsePacket -> context.getMessageDeliveryService().send(clientChannel, responsePacket));
            handlerResult.event().ifPresent(eventPublisher::publish);
        } catch (Exception e) {
            log.error("Error processing packet from {}: {}", clientChannel, e.getMessage());
        }
    }

    private void updateClientActivity(SocketChannel clientChannel) {
        Session session = context.getSession(clientChannel);
        if (session != null) {
            session.updateLastActivity();
        }
    }

    private void checkKeepAliveTimeouts() {
        var expiredSessions = context.getActiveSessions().entrySet().stream()
                .filter(entry -> entry.getValue().isKeepAliveExpired()).toList();
        expiredSessions.forEach(entry -> {
            var session = entry.getValue();
            log.warn("Keep Alive timeout for client: {}", session.getClientId());
            ofNullable(entry.getKey().keyFor(selector)).ifPresent(this::cleanupClient);
        });
    }

    private void cleanupClient(SelectionKey key) {
        var clientChannel = (SocketChannel) key.channel();
        eventPublisher.publish(new ConnectionLostEvent(clientChannel));
        try {
            key.cancel();
            var connection = connections.remove(clientChannel);
            if (connection != null) {
                connection.close();
            } else {
                clientChannel.close();
            }
        } catch (IOException e) {
            log.error("Error closing client channel: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        log.info("Shutting down broker...");
        running.set(false);
        if (selector != null) {
            selector.wakeup();
        }
        if (packetExecutor != null) {
            packetExecutor.close();
        }
    }

    public void stop() {
        try {
            context.persistSessions();
            if (selector != null)
                selector.close();
            if (serverChannel != null)
                serverChannel.close();
        } catch (IOException e) {
            log.error("Error closing broker: {}", e.getMessage());
        }
    }
}