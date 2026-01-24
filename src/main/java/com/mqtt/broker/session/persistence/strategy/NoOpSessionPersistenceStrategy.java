package com.mqtt.broker.session.persistence.strategy;

import com.mqtt.broker.session.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Slf4j
public class NoOpSessionPersistenceStrategy implements SessionPersistenceStrategy {

    @Override
    public void save(Collection<Session> sessions) {
    }

    @Override
    public Map<String, Session> load() {
        return Collections.emptyMap();
    }
}
