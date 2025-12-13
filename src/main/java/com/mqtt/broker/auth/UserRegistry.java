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
                if (user.permissions() != null) {
                    user.permissions().forEach(permission -> validateTopic(permission.topic()));
                }
                this.users.put(user.username(), user);
            });
            log.info("Loaded {} users from {}", users.size(), USERS_FILE);
        } catch (JsonProcessingException e) {
            log.error("Error parsing users file: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error reading users file: {}", e.getMessage());
        }
    }

    public User getUserBy(String username) {
        return users.get(username);
    }

    public boolean hasUsers() {
        return !users.isEmpty();
    }
}
