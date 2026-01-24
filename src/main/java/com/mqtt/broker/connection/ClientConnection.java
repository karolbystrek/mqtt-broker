package com.mqtt.broker.connection;

import com.mqtt.broker.session.Session;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Getter
public class ClientConnection implements AutoCloseable {

    private final SocketChannel channel;
    private final ByteBuffer buffer;
    @Setter
    private Session session;

    public ClientConnection(SocketChannel channel) {
        this.channel = channel;
        this.buffer = ByteBuffer.allocate(8192);
    }

    public int read() throws IOException {
        return channel.read(buffer);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    public String getRemoteAddress() {
        try {
            return channel.getRemoteAddress().toString();
        } catch (IOException e) {
            return "unknown";
        }
    }
}
