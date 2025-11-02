package com.mqtt.broker.packet;

import lombok.Getter;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBCOMP;

@Getter
public final class PubCompPacket extends MqttPacket {

    private final int packetIdentifier;

    public PubCompPacket(MqttFixedHeader fixedHeader, int packetIdentifier) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PUBCOMP) {
            throw invalidPacketType(PubCompPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
        this.packetIdentifier = packetIdentifier;
    }
}
