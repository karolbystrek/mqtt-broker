package com.mqtt.broker.auth.strategy;

public interface AuthorizationStrategy {
    boolean authenticate(String username, String password);

    boolean canSubscribe(String username, String topicFilter);

    boolean canPublish(String username, String topic);
}
