package com.mqtt.broker.event;

import java.util.ArrayList;
import java.util.List;

public class BrokerEventPublisher {
    private final List<EventListener> listeners = new ArrayList<>();

    public void publish(BrokerEvent event) {
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<EventListener> listeners = new ArrayList<>();

        public Builder addListener(EventListener listener) {
            listeners.add(listener);
            return this;
        }

        public BrokerEventPublisher build() {
            BrokerEventPublisher publisher = new BrokerEventPublisher();
            publisher.listeners.addAll(this.listeners);
            return publisher;
        }
    }
}
