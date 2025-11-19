package com.mqtt.broker.service;

import com.mqtt.broker.Session;
import com.mqtt.broker.encoder.MqttPacketEncoder;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@Slf4j
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
            log.error("Failed to deliver pending message: " + e.getMessage());
        }
    }
}
