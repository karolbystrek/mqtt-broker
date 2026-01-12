package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.UNSUBACK;

public record UnsubAckPacket(
        MqttFixedHeader fixedHeader,
        int packetIdentifier
) implements MqttPacket {

    public UnsubAckPacket {
        if (fixedHeader.packetType() != UNSUBACK) {
            throw invalidPacketType(UnsubAckPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
    }
}
