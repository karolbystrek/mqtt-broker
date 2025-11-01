package com.mqtt.broker;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.newSetFromMap;

public class Session {

    private final String clientId;
    private final Set<String> subscriptions;
    private final boolean cleanSession;

    public Session(String clientId, boolean cleanSession) {
        this.clientId = clientId;
        this.subscriptions = newSetFromMap(new ConcurrentHashMap<>());
        this.cleanSession = cleanSession;
    }

    public void AddSubscription(String topic) {
        subscriptions.add(topic);
    }

    public void RemoveSubscription(String topic) {
        subscriptions.remove(topic);
    }
}
