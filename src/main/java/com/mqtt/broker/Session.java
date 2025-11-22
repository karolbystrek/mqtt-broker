package com.mqtt.broker;

import com.mqtt.broker.packet.MqttQoS;
import com.mqtt.broker.packet.PublishPacket;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;

public class Session {

    private static final int MAX_PENDING_MESSAGES = 1000;

    @Getter
    final String clientId;
    private final Map<String, MqttQoS> subscriptions;
    @Getter
    private final boolean isCleanSession;
    private volatile int keepAliveSeconds;
    private final AtomicLong lastActivityTimestamp;
    private final Queue<PublishPacket> pendingMessages;
    private final Map<Integer, PublishPacket> incomingMessages;

    @Setter @Getter
    private WillMessage willMessage;

    public record WillMessage(String topic, String message, boolean retain, int qos) {}

    public Session(String clientId, boolean isCleanSession, int keepAliveSeconds) {
        this.clientId = clientId;
        this.subscriptions = new ConcurrentHashMap<>();
        this.isCleanSession = isCleanSession;
        this.keepAliveSeconds = keepAliveSeconds;
        this.lastActivityTimestamp = new AtomicLong(currentTimeMillis());
        this.pendingMessages = new ConcurrentLinkedQueue<>();
        this.incomingMessages = new ConcurrentHashMap<>();
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

    public Stream<PublishPacket> getPendingMessagesStream() {
        return pendingMessages.stream();
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
