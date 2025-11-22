package com.mqtt.broker.event;

public interface EventListener {
    void onEvent(BrokerEvent event);
}
