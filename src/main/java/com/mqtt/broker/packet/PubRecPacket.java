package com.mqtt.broker.packet;

import lombok.Getter;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBREC;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

@Getter
public final class PubRecPacket extends MqttPacket {

    private final int packetIdentifier;

    public PubRecPacket(MqttFixedHeader fixedHeader, int packetIdentifier) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PUBREC) {
            throw invalidPacketType(PubRecPacket.class);
        }
        this.packetIdentifier = packetIdentifier;
    }
}
