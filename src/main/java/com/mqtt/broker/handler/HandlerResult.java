package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;

import java.util.Optional;
import com.mqtt.broker.event.BrokerEvent;

public record HandlerResult(Optional<MqttPacket> responsePacket, Optional<BrokerEvent> event) {

    public static HandlerResult withResponse(MqttPacket packet) {
        return new HandlerResult(Optional.of(packet), Optional.empty());
    }

    public static HandlerResult withResponseAndEvent(MqttPacket packet, BrokerEvent event) {
        return new HandlerResult(Optional.of(packet), Optional.of(event));
    }

    public static HandlerResult withEvent(BrokerEvent event) {
        return new HandlerResult(Optional.empty(), Optional.of(event));
    }

    public static HandlerResult empty() {
        return new HandlerResult(Optional.empty(), Optional.empty());
    }
}
