package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.PUBACK;

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
