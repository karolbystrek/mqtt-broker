package com.mqtt.broker.authorization.strategy;

import com.mqtt.broker.packet.ConnectPacket;

public interface AuthorizationStrategy {
    boolean authenticate(ConnectPacket packet);

    boolean canSubscribe(String username, String topicFilter);

    boolean canPublish(String username, String topic);
}
