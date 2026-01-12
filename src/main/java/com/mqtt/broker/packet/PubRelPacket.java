package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.PUBREL;

public record PubRelPacket(MqttFixedHeader fixedHeader, int packetIdentifier) implements MqttPacket {
    public PubRelPacket {
        if (fixedHeader.packetType() != PUBREL) {
            throw invalidPacketType(PubRelPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
    }
}
