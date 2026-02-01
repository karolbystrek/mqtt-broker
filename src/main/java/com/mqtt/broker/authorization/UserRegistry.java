package com.mqtt.broker.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class UserRegistry {

    private final File usersFile;
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserRegistry(String filePath) {
        this.usersFile = new File(filePath);
        loadUsers();
    }

    private void loadUsers() {
        if (!Files.exists(usersFile.toPath())) {
            log.warn("Users file not found: {}", usersFile);
            return;
        }

        var objectMapper = new ObjectMapper();
        try {
            List<User> users = objectMapper.readValue(usersFile, new TypeReference<>() {
            });
            users.forEach(user -> this.users.put(user.username(), user));
            log.info("Loaded {} users from {}", users.size(), usersFile);
        } catch (JsonProcessingException e) {
            log.error("Error parsing users file: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error reading users file: {}", e.getMessage());
        }
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }
}
