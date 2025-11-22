package com.mqtt.broker.event;

import java.util.ArrayList;
import java.util.List;

public class BrokerEventPublisher {
    private final List<BrokerEventListener> listeners = new ArrayList<>();

    public void addListener(BrokerEventListener listener) {
        listeners.add(listener);
    }

    public void publish(BrokerEvent event) {
        for (BrokerEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }
}
