package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.UNSUBACK;

@Getter
public final class UnsubAckPacket extends MqttPacket {

    private final int packetIdentifier;

    public UnsubAckPacket(MqttFixedHeader fixedHeader, int packetIdentifier) {
        super(fixedHeader);
        if (fixedHeader.packetType() != UNSUBACK) {
            throw invalidPacketType(UnsubAckPacket.class);
        }
        this.packetIdentifier = packetIdentifier;
    }
}
