package com.mqtt.broker.auth;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class UserRegistry {

    private final Map<String, String> users = new ConcurrentHashMap<>();

    public UserRegistry(String usersFilePath) {
        loadUsers(usersFilePath);
    }

    private void loadUsers(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                } else {
                    log.warn("Invalid user entry in {}: {}", filePath, line);
                }
            }
            log.info("Loaded {} users from {}", users.size(), filePath);
        } catch (IOException e) {
            log.warn("Could not load users from {}: {}", filePath, e.getMessage());
        }
    }

    public boolean validate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String storedPassword = users.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }
}
