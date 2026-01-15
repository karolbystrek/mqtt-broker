package com.mqtt.broker.connection;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public interface ConnectionListener {
    ServerSocketChannel setup() throws IOException;
    void accept(SelectionKey key) throws IOException;
}
