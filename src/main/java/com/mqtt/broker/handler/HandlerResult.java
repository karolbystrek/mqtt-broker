package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;

import java.util.Optional;

public record HandlerResult(Optional<MqttPacket> responsePacket, Optional<PostConnectionAction> postAction) {

    public static HandlerResult withResponse(MqttPacket packet) {
        return new HandlerResult(Optional.of(packet), Optional.empty());
    }

    public static HandlerResult withResponseAndAction(MqttPacket packet, PostConnectionAction action) {
        return new HandlerResult(Optional.of(packet), Optional.of(action));
    }

    public static HandlerResult withAction(PostConnectionAction action) {
        return new HandlerResult(Optional.empty(), Optional.of(action));
    }

    public static HandlerResult empty() {
        return new HandlerResult(Optional.empty(), Optional.empty());
    }
}
