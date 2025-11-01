package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import java.util.List;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.UNSUBSCRIBE;
import static java.util.List.copyOf;

@Getter
public final class UnsubscribePacket extends MqttPacket {

    private final int packetIdentifier;
    private final List<String> topicFilters;

    public UnsubscribePacket(MqttFixedHeader fixedHeader, int packetIdentifier, List<String> topicFilters) {
        super(fixedHeader);
        if (fixedHeader.packetType() != UNSUBSCRIBE) {
            throw invalidPacketType(UnsubscribePacket.class);
        }
        this.packetIdentifier = packetIdentifier;
        this.topicFilters = copyOf(topicFilters);
    }
}
