package com.mqtt.broker.packet;

import java.util.List;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.SUBACK;
import static java.util.List.copyOf;

public record SubAckPacket(
        MqttFixedHeader fixedHeader,
        int packetIdentifier,
        List<Integer> grantedQosLevels
) implements MqttPacket {

    public SubAckPacket {
        if (fixedHeader.packetType() != SUBACK) {
            throw invalidPacketType(SubAckPacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
        grantedQosLevels = copyOf(grantedQosLevels);
    }
}
