package com.mqtt.broker.event;

import com.mqtt.broker.packet.PublishPacket;

public record PublishEvent(PublishPacket packet) implements BrokerEvent {
}
