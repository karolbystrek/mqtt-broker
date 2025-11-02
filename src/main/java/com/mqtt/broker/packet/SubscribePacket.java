package com.mqtt.broker.packet;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.SUBSCRIBE;

@Getter
@ToString
public final class SubscribePacket extends MqttPacket {

    private final int packetIdentifier;
    private final List<Subscription> subscriptions;

    public SubscribePacket(MqttFixedHeader fixedHeader, int packetIdentifier, List<Subscription> subscriptions) {
        super(fixedHeader);
        if (fixedHeader.packetType() != SUBSCRIBE) {
            throw invalidPacketType(SubscribePacket.class);
        }
        if (packetIdentifier < 0 || packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }
        this.packetIdentifier = packetIdentifier;
        this.subscriptions = subscriptions;
    }

    public record Subscription(String topicFilter, MqttQoS qos) {
    }
}
