package com.mqtt.broker.event;

public interface EventPublisher {
    void publish(BrokerEvent event);
}
