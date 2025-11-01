package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import com.mqtt.broker.MqttQoS;
import lombok.Getter;

import java.util.List;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.SUBSCRIBE;

@Getter
public final class SubscribePacket extends MqttPacket {

    private final int packetIdentifier;
    private final List<Subscription> subscriptions;

    public SubscribePacket(MqttFixedHeader fixedHeader, int packetIdentifier, List<Subscription> subscriptions) {
        super(fixedHeader);
        if (fixedHeader.packetType() != SUBSCRIBE) {
            throw invalidPacketType(SubscribePacket.class);
        }
        this.packetIdentifier = packetIdentifier;
        this.subscriptions = subscriptions;
    }

    public record Subscription(String topicFilter, MqttQoS qos) {
    }
}
