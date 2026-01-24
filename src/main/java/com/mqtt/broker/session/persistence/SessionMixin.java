package com.mqtt.broker.session.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mqtt.broker.packet.MqttQoS;
import com.mqtt.broker.packet.PublishPacket;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SessionMixin {

    @JsonProperty("nextPacketId")
    private AtomicInteger packetIdGenerator;

    public SessionMixin(
            @JsonProperty("clientId") String clientId,
            @JsonProperty("username") String username,
            @JsonProperty("cleanSession") boolean isCleanSession,
            @JsonProperty("keepAliveSeconds") int keepAliveSeconds,
            @JsonProperty("subscriptions") Map<String, MqttQoS> subscriptions,
            @JsonProperty("pendingMessages") List<PublishPacket> pendingMessages
    ) {
    }

    @JsonProperty("pendingMessages")
    abstract Queue<PublishPacket> getPendingMessages();

    // Ignore fields that should not be serialized
    @JsonIgnore
    abstract Object getKeepAliveSeconds();

    @JsonIgnore
    abstract Object getWillMessage();

    @JsonIgnore
    abstract Object isKeepAliveExpired();

    @JsonIgnore
    abstract Object isCleanSession();

    @JsonIgnore
    abstract Object getLastActivityTimestamp();

    @JsonIgnore
    abstract Object getPacketIdGenerator();

    @JsonIgnore
    abstract Object getIncomingMessages();
}
