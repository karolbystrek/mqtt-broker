package com.mqtt.broker;

import com.mqtt.broker.connection.ClientConnection;
import com.mqtt.broker.connection.ServerListener;
import com.mqtt.broker.decoder.ProtocolDecoder;
import com.mqtt.broker.event.ConnectionLostEvent;
import com.mqtt.broker.event.EventPublisher;
import com.mqtt.broker.interceptor.Pipeline;
import com.mqtt.broker.session.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Optional.ofNullable;

@Slf4j
@RequiredArgsConstructor
public class Broker implements AutoCloseable {

    private static final int KEEP_ALIVE_CHECK_INTERVAL_MS = 1000;

    private final AtomicBoolean running = new AtomicBoolean(true);

    private final BrokerContext context;
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final ProtocolDecoder packetDecoder;
    private final EventPublisher eventPublisher;
    private final ServerListener serverListener;
    private final ExecutorService packetExecutor;
    private final Pipeline pipeline;
    private final Map<SocketChannel, ClientConnection> connections;

    public static BrokerBuilder builder() {
        return new BrokerBuilder();
    }

    public void start() {
        try {
            while (running.get()) {
                selector.select(KEEP_ALIVE_CHECK_INTERVAL_MS);
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
            var packet = packetDecoder.decode(buffer);
            if (packet == null) {
                break;
            }
            packetExecutor.submit(() -> pipeline.process(clientChannel, packet));
        }
        buffer.compact(); // compact the buffer to preserve incomplete data
    }

    private void checkKeepAliveTimeouts() {
        var expiredSessions = context.getSessionManager().getActiveSessions().stream()
                .filter(Session::isKeepAliveExpired).toList();
        expiredSessions.forEach(session -> {
            log.warn("Keep Alive timeout for client: {}", session.getClientId());
            var clientChannel = context.getSessionManager().getClientChannel(session.getClientId());
            if (clientChannel != null) {
                ofNullable(clientChannel.keyFor(selector)).ifPresent(this::cleanupClient);
            }
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
            context.getSessionManager().persistSessions();
            if (selector != null)
                selector.close();
            if (serverChannel != null)
                serverChannel.close();
        } catch (IOException e) {
            log.error("Error closing broker: {}", e.getMessage());
        }
    }
}
