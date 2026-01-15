package com.mqtt.broker.connection;

import com.mqtt.broker.config.BrokerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

@Slf4j
@RequiredArgsConstructor
public class ServerListener {

    private final Selector selector;
    private final BrokerConfiguration config;
    private final Map<SocketChannel, ClientConnection> connections;

    public ServerSocketChannel setup() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(config.getHost(), config.getPort()));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, OP_ACCEPT);
        return serverChannel;
    }

    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        if (clientChannel != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, OP_READ);

            ClientConnection connection = new ClientConnection(clientChannel);
            connections.put(clientChannel, connection);

            log.info("Accepted new connection from {}", clientChannel.getRemoteAddress());
        }
    }
}
