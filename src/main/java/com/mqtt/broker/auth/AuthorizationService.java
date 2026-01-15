package com.mqtt.broker.auth;

import com.mqtt.broker.auth.strategy.AuthorizationStrategy;
import com.mqtt.broker.packet.ConnectPacket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationService {

    private final AuthorizationStrategy strategy;

    public AuthorizationService(AuthorizationStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean authenticate(ConnectPacket packet) {
        return strategy.authenticate(packet);
    }

    public boolean canSubscribe(String username, String topic) {
        return strategy.canSubscribe(username, topic);
    }

    public boolean canPublish(String username, String topic) {
        return strategy.canPublish(username, topic);
    }
}