package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.PUBREL;

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
