package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.PUBCOMP;

public record PubCompPacket(MqttFixedHeader fixedHeader, int packetIdentifier) implements MqttPacket {
    public PubCompPacket {
        if (fixedHeader.packetType() != PUBCOMP) {
            throw invalidPacketType(PubCompPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
    }
}
