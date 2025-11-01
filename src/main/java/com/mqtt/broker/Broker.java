package com.mqtt.broker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class Broker implements AutoCloseable {

    private static final String HOST = "localhost";
    private static final int PORT = 1883;

    private final Selector selector;
    private final ServerSocketChannel serverChannel;

    public Broker() throws IOException {
        this.selector = Selector.open();
        this.serverChannel = setupServer(selector);
    }

    public void start() {
        System.out.println("Broker started on " + HOST + ":" + PORT);
        try {
            while (true) {
                selector.select();
                var keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    var key = keyIterator.next();
                    try {
                        if (key.isAcceptable()) {
                            acceptConnection(key);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (IOException e) {
                        System.err.println("Error handling client" + key + ": " + e.getMessage());
                        key.cancel();
                        key.channel().close();
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
        var buffer = ByteBuffer.allocate(1024); // TODO: A real-world broker needs to handle buffer-splitting and larger packets
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            System.out.println("Connection close by: " + clientChannel.getRemoteAddress());
            clientChannel.close();
            key.cancel();
            return;
        }
        buffer.flip();
        System.out.println("Received message from " + clientChannel.getRemoteAddress());
        buffer.clear();
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        var serverChannel = (ServerSocketChannel) key.channel();
        var clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, OP_READ);
        System.out.println("Accepted new connection from " + clientChannel.getRemoteAddress());
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
