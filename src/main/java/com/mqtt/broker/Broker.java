package com.mqtt.broker;

import com.mqtt.broker.decoder.MqttPacketDecoder;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.handler.PacketHandlerFactory;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.service.PendingMessageDeliveryService;
import com.mqtt.broker.trie.TopicTree;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class Broker implements AutoCloseable {

    private static final String HOST = "localhost";
    private static final int PORT = 1883;
    private static final long KEEP_ALIVE_CHECK_INTERVAL_MS = 1_000;

    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private final MqttPacketDecoder decoder;
    private final MqttPacketEncoder encoder;
    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, SocketChannel> clientIdToChannel;
    private final Map<String, Session> persistentSessions;
    private final TopicTree topicTree;
    private final PacketHandlerFactory handlerFactory;
    private final Map<SocketChannel, ByteBuffer> clientBuffers;
    private final PendingMessageDeliveryService pendingMessageService;

    public Broker() throws IOException {
        this.selector = Selector.open();
        this.serverChannel = setupServer(selector);
        this.decoder = new MqttPacketDecoder();
        this.encoder = new MqttPacketEncoder();
        this.activeSessions = new ConcurrentHashMap<>();
        this.clientIdToChannel = new ConcurrentHashMap<>();
        this.persistentSessions = new ConcurrentHashMap<>();
        this.topicTree = new TopicTree();
        this.pendingMessageService = new PendingMessageDeliveryService(encoder);
        this.handlerFactory = new PacketHandlerFactory(activeSessions, persistentSessions, topicTree, clientIdToChannel, pendingMessageService);
        this.clientBuffers = new ConcurrentHashMap<>();
    }

    public void start() {
        System.out.println("Broker started on " + HOST + ":" + PORT);
        try {
            while (true) {
                selector.select(KEEP_ALIVE_CHECK_INTERVAL_MS);

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
                        System.err.println("Error handling client" + key + ": " + e.getMessage());
                        cleanupClient(key);
                    } finally {
                        keyIterator.remove();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Broker encountered an error: " + e.getMessage());
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
            System.out.println("Connection closed by: " + clientChannel.getRemoteAddress());
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

        if (handlerResult.responsePacket().isPresent()) {
            var responseBuffer = encoder.encode(handlerResult.responsePacket().get());
            while (responseBuffer.hasRemaining()) {
                clientChannel.write(responseBuffer);
            }
        }

        if (handlerResult.postAction().isPresent()) {
            handlerResult.postAction().get().execute(clientChannel);
        }
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        var serverChannel = (ServerSocketChannel) key.channel();
        var clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, OP_READ);

        clientBuffers.put(clientChannel, ByteBuffer.allocate(8192));

        System.out.println("Accepted new connection from " + clientChannel.getRemoteAddress());
    }

    private void updateClientActivity(SocketChannel clientChannel) {
        Session session = activeSessions.get(clientChannel);
        if (session != null) {
            session.updateLastActivity();
        }
    }

    private void checkKeepAliveTimeouts() {
        activeSessions.entrySet().stream()
                .filter(entry -> entry.getValue().isKeepAliveExpired())
                .forEach(entry -> {
                    Session session = entry.getValue();
                    System.err.println("Keep Alive timeout for client: " + session.getClientId());
                    SelectionKey key = entry.getKey().keyFor(selector);
                    if (key != null) cleanupClient(key);
                });
    }

    private void cleanupClient(SelectionKey key) {
        var clientChannel = (SocketChannel) key.channel();

        Session session = activeSessions.get(clientChannel);
        if (session != null) {
            if (session.isCleanSession()) {
                topicTree.removeAllSubscriptionsFor(session.getClientId());
            } else {
                persistentSessions.put(session.getClientId(), session);
                System.out.println("Saved persistent session for client: " + session.getClientId());
            }
            clientIdToChannel.remove(session.getClientId());
        }

        try {
            key.cancel();
            clientChannel.close();
        } catch (IOException e) {
            System.err.println("Error closing client channel: " + e.getMessage());
        } finally {
            clientBuffers.remove(clientChannel);
            activeSessions.remove(clientChannel);
        }
    }

    @Override
    public void close() {
        System.out.println("Shutting down broker...");
        try {
            if (selector != null) selector.close();
            if (serverChannel != null) serverChannel.close();
        } catch (IOException e) {
            System.err.println("Error closing broker: " + e.getMessage());
        }
    }

    private static ServerSocketChannel setupServer(Selector selector) throws IOException {
        var serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(HOST, PORT));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, OP_ACCEPT);
        return serverChannel;
    }
}
