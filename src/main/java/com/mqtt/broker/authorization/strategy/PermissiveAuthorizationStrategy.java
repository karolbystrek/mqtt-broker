package com.mqtt.broker.authorization.strategy;

import com.mqtt.broker.packet.ConnectPacket;

public class PermissiveAuthorizationStrategy implements AuthorizationStrategy {
    @Override
    public boolean authenticate(ConnectPacket packet) {
        return true;
    }

    @Override
    public boolean canSubscribe(String username, String topicFilter) {
        return true;
    }

    @Override
    public boolean canPublish(String username, String topic) {
        return true;
    }
}
