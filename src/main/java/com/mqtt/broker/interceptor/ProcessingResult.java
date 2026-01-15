package com.mqtt.broker.interceptor;

import com.mqtt.broker.event.BrokerEvent;
import com.mqtt.broker.packet.MqttPacket;

import java.util.Optional;

public record ProcessingResult(Optional<MqttPacket> responsePacket, Optional<BrokerEvent> event) {

    public static ProcessingResult withResponse(MqttPacket packet) {
        return new ProcessingResult(Optional.of(packet), Optional.empty());
    }

    public static ProcessingResult withResponseAndEvent(MqttPacket packet, BrokerEvent event) {
        return new ProcessingResult(Optional.of(packet), Optional.of(event));
    }

    public static ProcessingResult withEvent(BrokerEvent event) {
        return new ProcessingResult(Optional.empty(), Optional.of(event));
    }

    public static ProcessingResult empty() {
        return new ProcessingResult(Optional.empty(), Optional.empty());
    }
}
