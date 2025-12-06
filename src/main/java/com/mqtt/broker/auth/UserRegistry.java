package com.mqtt.broker.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mqtt.broker.trie.TopicValidator.validateTopic;

@Slf4j
public class UserRegistry {

    private static final File USERS_FILE = new File("users.json");

    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserRegistry() {
        loadUsers();
    }

    private void loadUsers() {
        if (!Files.exists(USERS_FILE.toPath())) {
            log.warn("Users file not found: {}", USERS_FILE);
            return;
        }

        var objectMapper = new ObjectMapper();
        try {
            List<User> users = objectMapper.readValue(USERS_FILE, new TypeReference<>() {
            });
            users.forEach(user -> {
                user.permissions().forEach(permission -> validateTopic(permission.topic()));
                this.users.put(user.username(), user);
            });
            log.info("Loaded {} users from {}", users.size(), USERS_FILE);
        } catch (JsonProcessingException e) {
            log.error("Error parsing users file: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error reading users file: {}", e.getMessage());
        }
    }

    public boolean authenticate(String username, String password) {
        if (users.isEmpty()) { // No users registered, allow all
            return true;
        }

        if (username == null || password == null) {
            return false;
        }
        var user = users.get(username);
        return user != null && user.password().equals(password);
    }

    public boolean canSubscribe(String username, String topicFilter) {
        if (users.isEmpty()) {
            return true;
        }
        if (username == null) {
            return false;
        }
        var user = users.get(username);
        if (user == null) {
            return false;
        }
        return user.permissions().stream()
                .anyMatch(p -> (p.access() == User.TopicPermission.PermissionLevel.READ || p.access() == User.TopicPermission.PermissionLevel.READ_WRITE)
                        && matches(p.topic(), topicFilter));
    }

    public boolean canPublish(String username, String topic) {
        if (users.isEmpty()) {
            return true;
        }
        if (username == null) {
            return false;
        }
        var user = users.get(username);
        if (user == null) {
            return false;
        }
        return user.permissions().stream()
                .anyMatch(p -> (p.access() == User.TopicPermission.PermissionLevel.WRITE || p.access() == User.TopicPermission.PermissionLevel.READ_WRITE)
                        && matches(p.topic(), topic));
    }

    private boolean matches(String permissionPattern, String topic) {
        if (permissionPattern.equals(topic)) {
            return true;
        }
        if (permissionPattern.equals("#")) {
            return true;
        }
        if (permissionPattern.equals("+")) {
            return !topic.contains("/");
        }

        String[] patternLevels = permissionPattern.split("/");
        String[] topicLevels = topic.split("/");

        for (int i = 0; i < patternLevels.length; i++) {
            String left = patternLevels[i];

            if (i >= topicLevels.length) {
                return false;
            }
            String right = topicLevels[i];

            if (left.equals("#")) {
                return true;
            }
            if (!left.equals("+") && !left.equals(right)) {
                return false;
            }
        }

        return patternLevels.length == topicLevels.length;
    }
}
