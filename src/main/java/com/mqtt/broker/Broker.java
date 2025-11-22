package com.mqtt.broker;

import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.decoder.MqttPacketDecoder;
import com.mqtt.broker.handler.PacketHandlerFactory;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.packet.PublishPacket.PublishVariableHeader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;
import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.util.Optional.ofNullable;

import com.mqtt.broker.event.BrokerEventPublisher;
import com.mqtt.broker.event.BrokerEventListener;

@Slf4j
public class Broker implements AutoCloseable {

    private final BrokerConfiguration config;
    private final BrokerContext context;

    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final MqttPacketDecoder decoder;
    private final PacketHandlerFactory handlerFactory;
    private final Map<SocketChannel, ByteBuffer> clientBuffers;
    private final BrokerEventPublisher eventPublisher;

    public Broker(BrokerConfiguration config, BrokerContext context) throws IOException {
        this.config = config;
        this.context = context;
        this.selector = Selector.open();
        this.serverChannel = setupServer(selector, config);
        this.decoder = new MqttPacketDecoder();
        this.handlerFactory = new PacketHandlerFactory(context);
        this.clientBuffers = new ConcurrentHashMap<>();
        this.eventPublisher = new BrokerEventPublisher();
        eventPublisher.addListener(new BrokerEventListener(context));
    }

    public void start() {
        log.info("Broker started on {}:{}", config.getServer().getHost(), config.getServer().getPort());
        try {
            while (true) {
                selector.select(config.getMqtt().getKeepAliveCheckIntervalMs());

                checkKeepAliveTimeouts();

                var keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    var key = keyIterator.next();
                    try {
                        if (key.isAcceptable()) {
                            acceptConnection(key);
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
        } finally {
            close();
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        var clientChannel = (SocketChannel) key.channel();
        var buffer = clientBuffers.get(clientChannel);
        if (buffer == null) {
            return;
        }
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            log.info("Connection closed by: {}", clientChannel.getRemoteAddress());
            cleanupClient(key);
            return;
        }

        buffer.flip(); // flip the buffer for reading

        while (buffer.hasRemaining()) {
            var optionalPacket = decoder.decode(buffer);
            if (optionalPacket.isEmpty()) {
                buffer.reset(); // incomplete packet, wait for more data
                break;
            }

            processPacket(clientChannel, optionalPacket.get());
        }

        buffer.compact(); // compact the buffer to preserve incomplete data
    }

    private void processPacket(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        updateClientActivity(clientChannel);

        var handler = handlerFactory.getHandler(packet.getFixedHeader().packetType());
        var handlerResult = handler.handle(clientChannel, packet);

        handlerResult.responsePacket().ifPresent(
            responsePacket -> context.getPacketSender().send(clientChannel, responsePacket)
        );

        handlerResult.event().ifPresent(eventPublisher::publish);
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        var serverChannel = (ServerSocketChannel) key.channel();
        var clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, OP_READ);

        clientBuffers.put(clientChannel, ByteBuffer.allocate(8192));

        log.info("Accepted new connection from {}", clientChannel.getRemoteAddress());
    }

    private void updateClientActivity(SocketChannel clientChannel) {
        Session session = context.getSession(clientChannel);
        if (session != null) {
            session.updateLastActivity();
        }
    }

    private void checkKeepAliveTimeouts() {
        var expiredSessions = context.getActiveSessions().entrySet().stream()
                .filter(entry -> entry.getValue().isKeepAliveExpired())
                .toList();
        expiredSessions.forEach(entry -> {
            var session = entry.getValue();
            log.warn("Keep Alive timeout for client: {}", session.getClientId());
            ofNullable(entry.getKey().keyFor(selector))
                    .ifPresent(this::cleanupClient);
        });
    }

    private void cleanupClient(SelectionKey key) {
        var clientChannel = (SocketChannel) key.channel();

        Session session = context.getSession(clientChannel);
        if (session != null) {
            if (session.getWillMessage() != null) {
                log.info("Client {} disconnected unexpectedly. Sending Will Message.", session.getClientId());
                var willMessage = session.getWillMessage();
                
                byte flags = 0;
                flags |= (willMessage.qos() << 1);
                if (willMessage.retain()) {
                    flags |= 1;
                }
                
                var fixedHeader = new MqttFixedHeader(PUBLISH, flags, 0);
                int packetId = 1;
                if (willMessage.qos() > 0) {
                    packetId = 2; // TODO: Use a proper Packet ID generator
                }
                
                var variableHeader = new PublishVariableHeader(willMessage.topic(), packetId);
                var payload = willMessage.message().getBytes(UTF_8);
                
                var publishPacket = new PublishPacket(fixedHeader, variableHeader, payload);
                
                context.getMessageDispatcher().dispatch(publishPacket);
            }
            
            if (session.isCleanSession()) {
                context.getTopicTree().removeAllSubscriptionsFor(session.getClientId());
            } else {
                context.savePersistentSession(session.getClientId(), session);
                log.info("Saved persistent session for client: {}", session.getClientId());
            }
            context.removeSession(clientChannel);
        }

        try {
            key.cancel();
            clientChannel.close();
        } catch (IOException e) {
            log.error("Error closing client channel: {}", e.getMessage());
        } finally {
            clientBuffers.remove(clientChannel);
            context.removeSession(clientChannel);
        }
    }

    @Override
    public void close() {
        log.info("Shutting down broker...");
        try {
            if (selector != null) selector.close();
            if (serverChannel != null) serverChannel.close();
        } catch (IOException e) {
            log.error("Error closing broker: {}", e.getMessage());
        }
    }

    private static ServerSocketChannel setupServer(Selector selector, BrokerConfiguration config) throws IOException {
        var serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(config.getServer().getHost(), config.getServer().getPort()));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, OP_ACCEPT);
        return serverChannel;
    }
}
