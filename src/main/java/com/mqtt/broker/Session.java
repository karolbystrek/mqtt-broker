package com.mqtt.broker;

import com.mqtt.broker.packet.MqttQoS;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;

@Getter
public class Session {

    private final String clientId;
    private final Map<String, MqttQoS> subscriptions;
    private final boolean cleanSession;
    private volatile int keepAliveSeconds;
    private final AtomicLong lastActivityTimestamp;

    public Session(String clientId, boolean cleanSession, int keepAliveSeconds) {
        this.clientId = clientId;
        this.subscriptions = new ConcurrentHashMap<>();
        this.cleanSession = cleanSession;
        this.keepAliveSeconds = keepAliveSeconds;
        this.lastActivityTimestamp = new AtomicLong(currentTimeMillis());
    }

    public void addSubscription(String topicFilter, MqttQoS qos) {
        subscriptions.put(topicFilter, qos);
    }

    public void removeSubscription(String topicFilter) {
        subscriptions.remove(topicFilter);
    }

    public void updateLastActivity() {
        lastActivityTimestamp.set(currentTimeMillis());
    }

    public void updateKeepAlive(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
        updateLastActivity(); // Reset the timer when keepAlive is updated
    }

    public boolean isKeepAliveExpired() {
        if (keepAliveSeconds == 0) {
            return false; // Keep alive disabled
        }

        long currentTime = currentTimeMillis();
        long lastActivity = lastActivityTimestamp.get();
        long maxIdleTimeMillis = (long) (keepAliveSeconds * 1.5 * 1000);

        return (currentTime - lastActivity) > maxIdleTimeMillis;
    }
}
