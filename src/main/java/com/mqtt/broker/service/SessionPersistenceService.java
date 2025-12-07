package com.mqtt.broker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqtt.broker.Session;
import com.mqtt.broker.persistence.json.MqttPersistenceModule;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class SessionPersistenceService {

    private static final String STORAGE_FILE = "mqtt-sessions.json";

    private final ObjectMapper objectMapper;
    private final File file;

    public SessionPersistenceService() {
        this.file = new File(STORAGE_FILE);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(INDENT_OUTPUT);
        this.objectMapper.registerModule(new MqttPersistenceModule());
    }

    public void save(Collection<Session> sessions) {
        try {
            objectMapper.writeValue(file, sessions);
            log.info("Persisted {} sessions to {}", sessions.size(), STORAGE_FILE);
        } catch (IOException e) {
            log.error("Failed to persist sessions", e);
        }
    }

    public Map<String, Session> load() {
        if (!file.exists() || file.length() == 0) {
            log.info("No session storage file found at {}, starting with empty sessions", STORAGE_FILE);
            return emptyMap();
        }
        try {
            List<Session> sessions = objectMapper.readValue(file, new TypeReference<>() {
            });
            log.info("Loaded {} sessions from {}", sessions.size(), STORAGE_FILE);
            return sessions.stream()
                    .collect(toMap(Session::getClientId, identity()));
        } catch (IOException e) {
            log.error("Failed to load sessions from {}", STORAGE_FILE, e);
            return emptyMap();
        }
    }
}
