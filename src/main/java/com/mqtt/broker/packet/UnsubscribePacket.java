package com.mqtt.broker.packet;

import java.util.List;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.UNSUBSCRIBE;
import static java.util.List.copyOf;

public record UnsubscribePacket(
        MqttFixedHeader fixedHeader,
        int packetIdentifier,
        List<String> topicFilters
) implements MqttPacket {

    public UnsubscribePacket {
        if (fixedHeader.packetType() != UNSUBSCRIBE) {
            throw invalidPacketType(UnsubscribePacket.class);
        }
        if (fixedHeader.flags() != 0b0010) {
            throw invalidPacketType(UnsubscribePacket.class);
        }
        topicFilters = copyOf(topicFilters);
    }
}
