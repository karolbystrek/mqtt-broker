package com.mqtt.broker.handler;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@FunctionalInterface
public interface PostConnectionAction {
    void execute(SocketChannel clientChannel) throws IOException;
}
