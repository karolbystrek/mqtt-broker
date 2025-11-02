package com.mqtt.broker.packet;

import lombok.Getter;
import lombok.ToString;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREL;

@Getter
@ToString
public final class PubRelPacket extends MqttPacket {

    private final int packetIdentifier;

    public PubRelPacket(MqttFixedHeader fixedHeader, int packetIdentifier) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PUBREL) {
            throw invalidPacketType(PubRelPacket.class);
        }
        this.packetIdentifier = packetIdentifier;
    }
}
