package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.PUBREC;

public record PubRecPacket(MqttFixedHeader fixedHeader, int packetIdentifier) implements MqttPacket {
    public PubRecPacket {
        if (fixedHeader.packetType() != PUBREC) {
            throw invalidPacketType(PubRecPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
    }
}
