package com.mqtt.broker.session.persistence.strategy;

import com.mqtt.broker.session.Session;

import java.util.Collection;
import java.util.Map;

public interface SessionPersistenceStrategy {
    void save(Collection<Session> sessions);

    Map<String, Session> load();
}
