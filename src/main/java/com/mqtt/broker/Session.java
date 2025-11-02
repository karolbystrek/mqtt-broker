package com.mqtt.broker;

import com.mqtt.broker.packet.MqttQoS;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Session {

    private final String clientId;
    private final Map<String, MqttQoS> subscriptions;
    private final boolean cleanSession;

    public Session(String clientId, boolean cleanSession) {
        this.clientId = clientId;
        this.subscriptions = new ConcurrentHashMap<>();
        this.cleanSession = cleanSession;
    }

    public void addSubscription(String topicFilter, MqttQoS qos) {
        subscriptions.put(topicFilter, qos);
    }

    public void removeSubscription(String topicFilter) {
        subscriptions.remove(topicFilter);
    }
}
