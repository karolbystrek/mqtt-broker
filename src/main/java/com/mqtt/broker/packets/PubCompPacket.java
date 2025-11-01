package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.PUBCOMP;

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
