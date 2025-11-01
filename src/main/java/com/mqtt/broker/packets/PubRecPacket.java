package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.PUBREC;

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
