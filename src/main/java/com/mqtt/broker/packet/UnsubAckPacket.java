package com.mqtt.broker.packet;

import lombok.Getter;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.UNSUBACK;

@Getter
public final class UnsubAckPacket extends MqttPacket {

    private final int packetIdentifier;

    public UnsubAckPacket(MqttFixedHeader fixedHeader, int packetIdentifier) {
        super(fixedHeader);
        if (fixedHeader.packetType() != UNSUBACK) {
            throw invalidPacketType(UnsubAckPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
        this.packetIdentifier = packetIdentifier;
    }
}
