package com.mqtt.broker.packet;

import lombok.Getter;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBCOMP;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

@Getter
public final class PubCompPacket extends MqttPacket {

    private final int packetIdentifier;

    public PubCompPacket(MqttFixedHeader fixedHeader, int packetIdentifier) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PUBCOMP) {
            throw invalidPacketType(PubCompPacket.class);
        }
        this.packetIdentifier = packetIdentifier;
    }
}
