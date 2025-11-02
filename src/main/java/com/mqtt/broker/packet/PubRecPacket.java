package com.mqtt.broker.packet;

import lombok.Getter;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREC;

@Getter
public final class PubRecPacket extends MqttPacket {

    private final int packetIdentifier;

    public PubRecPacket(MqttFixedHeader fixedHeader, int packetIdentifier) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PUBREC) {
            throw invalidPacketType(PubRecPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
        this.packetIdentifier = packetIdentifier;
    }
}
