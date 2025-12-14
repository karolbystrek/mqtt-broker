package com.mqtt.broker.auth;

import com.mqtt.broker.trie.TopicMatcher;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthorizationService {

    private final UserRegistry userRegistry = new UserRegistry();

    public boolean authenticate(String username, String password) {
        if (!userRegistry.hasUsers()) { // No users registered, allow all
            return true;
        }

        if (username == null || password == null) {
            return false;
        }
        var user = userRegistry.getUserBy(username);
        return user != null && user.password().equals(password);
    }

    public boolean canSubscribe(String username, String topicFilter) {
        if (!userRegistry.hasUsers()) {
            return true;
        }
        if (username == null) {
            return false;
        }
        var user = userRegistry.getUserBy(username);
        if (user == null) {
            return false;
        }
        if (user.permissions() == null || user.permissions().isEmpty()) {
            return true;
        }

        return user.permissions().stream()
                .anyMatch(p -> p.access().canRead() && TopicMatcher.matches(p.topic(), topicFilter));
    }

    public boolean canPublish(String username, String topic) {
        if (!userRegistry.hasUsers()) {
            return true;
        }
        if (username == null) {
            return false;
        }
        var user = userRegistry.getUserBy(username);
        if (user == null) {
            return false;
        }
        if (user.permissions() == null || user.permissions().isEmpty()) {
            return true;
        }

        return user.permissions().stream()
                .anyMatch(p -> p.access().canWrite() && TopicMatcher.matches(p.topic(), topic));
    }
}
