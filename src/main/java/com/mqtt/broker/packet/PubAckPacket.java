package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.PUBACK;

public record PubAckPacket(MqttFixedHeader fixedHeader, int packetIdentifier) implements MqttPacket {
    public PubAckPacket {
        if (fixedHeader.packetType() != PUBACK) {
            throw invalidPacketType(PubAckPacket.class);
        }
    }
}
