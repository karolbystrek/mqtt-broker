package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PublishPacket;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttQoS.AT_LEAST_ONCE;

@RequiredArgsConstructor
public class PublishPacketHandler implements MqttPacketHandler {

    private final MqttPacketEncoder encoder;

    @Override
    public void handle(SocketChannel clientChannel, MqttPacket packet) {
        if (!(packet instanceof PublishPacket publishPacket)) {
            return;
        }

        System.out.println("Handling PUBLISH packet for topic: " + publishPacket.getVariableHeader().topicName());

        publishPacket.getPacketIdentifier().ifPresent(packetId -> {
            try {
                if (publishPacket.getQosLevel() == AT_LEAST_ONCE) {
                    sendPubAck(clientChannel, packetId);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void sendPubAck(SocketChannel clientChannel, int packetId) throws IOException {
        MqttFixedHeader fixedHeader = new MqttFixedHeader(PUBACK, (byte) 0, 2);
        PubAckPacket pubAckPacket = new PubAckPacket(fixedHeader, packetId);

        var responseBuffer = encoder.encode(pubAckPacket);
        responseBuffer.flip();
        while (responseBuffer.hasRemaining()) {
            clientChannel.write(responseBuffer);
        }
        System.out.println("Sent PUBACK for Packet ID: " + packetId);
    }
}
