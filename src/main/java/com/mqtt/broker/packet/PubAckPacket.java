package com.mqtt.broker.packet;

import lombok.Getter;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBACK;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

@Getter
public final class PubAckPacket extends MqttPacket {

    private final int packetIdentifier;

    public PubAckPacket(MqttFixedHeader fixedHeader, int packetIdentifier) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PUBACK) {
            throw invalidPacketType(PubAckPacket.class);
        }
        this.packetIdentifier = packetIdentifier;
    }
}
