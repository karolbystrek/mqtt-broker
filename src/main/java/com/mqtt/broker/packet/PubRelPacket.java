package com.mqtt.broker.packet;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import static com.mqtt.broker.MqttControlPacketType.PUBREL;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

@Getter
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
