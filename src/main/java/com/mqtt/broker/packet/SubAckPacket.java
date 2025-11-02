package com.mqtt.broker.packet;

import lombok.Getter;

import java.util.List;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.SUBACK;
import static java.util.List.copyOf;

@Getter
public final class SubAckPacket extends MqttPacket {

    private final int packetIdentifier;
    private final List<Integer> grantedQosLevels;

    public SubAckPacket(MqttFixedHeader fixedHeader, int packetIdentifier, List<Integer> grantedQosLevels) {
        super(fixedHeader);
        if (fixedHeader.packetType() != SUBACK) {
            throw invalidPacketType(SubAckPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
        this.packetIdentifier = packetIdentifier;
        this.grantedQosLevels = copyOf(grantedQosLevels);
    }
}
