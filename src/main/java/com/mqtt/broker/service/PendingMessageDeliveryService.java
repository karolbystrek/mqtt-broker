package com.mqtt.broker.service;

import com.mqtt.broker.Session;
import com.mqtt.broker.encoder.MqttPacketEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PendingMessageDeliveryService {

    private final MqttPacketEncoder encoder;

    public PendingMessageDeliveryService(MqttPacketEncoder encoder) {
        this.encoder = encoder;
    }

    public void deliverPendingMessages(SocketChannel clientChannel, Session session) {
        session.getPendingMessagesStream()
                .map(encoder::encode)
                .forEach(encodedPacket -> sendMessage(clientChannel, encodedPacket));

        session.clearPendingMessages();
    }

    private void sendMessage(SocketChannel clientChannel, ByteBuffer encodedPacket) {
        try {
            while (encodedPacket.hasRemaining()) {
                clientChannel.write(encodedPacket);
            }
        } catch (IOException e) {
            System.err.println("Failed to deliver pending message: " + e.getMessage());
        }
    }
}
