package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PublishPacket;

import java.nio.channels.SocketChannel;
import java.util.Optional;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREC;
import static java.util.Optional.empty;

public class PublishPacketHandler implements MqttPacketHandler {

    @Override
    public Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) {
        if (!(packet instanceof PublishPacket publishPacket)) {
            return empty();
        }

        System.out.println("Handling PUBLISH packet: " + publishPacket);

        return switch (publishPacket.getQosLevel()) {
            case AT_LEAST_ONCE -> publishPacket.getPacketIdentifier()
                    .map(packetId -> {
                        var fixedHeader = new MqttFixedHeader(PUBACK, (byte) 0, 2);
                        return new PubAckPacket(fixedHeader, packetId);
                    });
            case EXACTLY_ONCE -> publishPacket.getPacketIdentifier()
                    .map(packetId -> {
                        var fixedHeader = new MqttFixedHeader(PUBREC, (byte) 0, 2);
                        return new PubRecPacket(fixedHeader, packetId);
                    });
            default -> empty(); // QoS 0 requires no response
        };
    }
}
