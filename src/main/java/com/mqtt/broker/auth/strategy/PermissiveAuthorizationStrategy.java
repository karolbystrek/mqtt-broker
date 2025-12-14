package com.mqtt.broker.auth.strategy;

public class PermissiveAuthorizationStrategy implements AuthorizationStrategy {
    @Override
    public boolean authenticate(String username, String password) {
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
