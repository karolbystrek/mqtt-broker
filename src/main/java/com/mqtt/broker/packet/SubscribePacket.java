package com.mqtt.broker.packet;

import java.util.List;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.SUBSCRIBE;
import static java.util.Collections.unmodifiableList;

public record SubscribePacket(
        MqttFixedHeader fixedHeader,
        int packetIdentifier,
        List<Subscription> subscriptions
) implements MqttPacket {

    public SubscribePacket {
        if (fixedHeader.packetType() != SUBSCRIBE) {
            throw invalidPacketType(SubscribePacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
        subscriptions = unmodifiableList(subscriptions);
    }

    public record Subscription(String topic, MqttQoS qos) {
    }
}
