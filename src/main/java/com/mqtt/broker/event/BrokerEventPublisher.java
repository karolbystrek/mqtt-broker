package com.mqtt.broker.event;

import java.util.ArrayList;
import java.util.List;

public class BrokerEventPublisher {
    private final List<EventListener> listeners = new ArrayList<>();

    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void publish(BrokerEvent event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}