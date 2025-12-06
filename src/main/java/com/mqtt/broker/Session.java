package com.mqtt.broker;

import com.mqtt.broker.packet.MqttQoS;
import com.mqtt.broker.packet.PublishPacket;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@Getter
public class Session {

    private static final int MAX_PENDING_MESSAGES = 1000;

    private final String clientId;
    @Getter
    private final String username;
    private final Map<String, MqttQoS> subscriptions;
    private final boolean isCleanSession;
    private volatile int keepAliveSeconds;
    private final AtomicLong lastActivityTimestamp;
    private final Queue<PublishPacket> pendingMessages;
    private final Map<Integer, PublishPacket> incomingMessages;
    private final AtomicInteger packetIdGenerator;
    @Setter
    private WillMessage willMessage;

    public record WillMessage(String topic, String message, boolean retain, int qos) {
    }

    public Session(String clientId, String username, boolean isCleanSession, int keepAliveSeconds) {
        this(clientId, username, isCleanSession, keepAliveSeconds, emptyMap(), emptyList());
    }

    public Session(String clientId, String username, boolean isCleanSession, int keepAliveSeconds,
                   Map<String, MqttQoS> subscriptions, List<PublishPacket> pendingMessages) {
        this.clientId = clientId;
        this.username = username;
        this.subscriptions = new ConcurrentHashMap<>(subscriptions);
        this.isCleanSession = isCleanSession;
        this.keepAliveSeconds = keepAliveSeconds;
        this.lastActivityTimestamp = new AtomicLong(currentTimeMillis());
        this.pendingMessages = new ConcurrentLinkedQueue<>(pendingMessages);
        this.incomingMessages = new ConcurrentHashMap<>();
        this.packetIdGenerator = new AtomicInteger(1);
    }

    public int nextPacketId() {
        int id = packetIdGenerator.getAndIncrement();
        if (id > 65535) {
            packetIdGenerator.set(1);
            id = 1;
        }
        return id;
    }

    public void storeIncomingMessage(PublishPacket packet) {
        incomingMessages.put(packet.getPacketIdentifier().orElseThrow(), packet);
    }

    public PublishPacket retrieveIncomingMessage(int packetId) {
        return incomingMessages.remove(packetId);
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

    public void enqueuePendingMessage(PublishPacket publishPacket) {
        if (pendingMessages.size() >= MAX_PENDING_MESSAGES) {
            pendingMessages.poll(); // Drop the oldest message
        }
        pendingMessages.add(publishPacket);
    }

    public void clearPendingMessages() {
        pendingMessages.clear();
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
