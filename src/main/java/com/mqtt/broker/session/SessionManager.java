package com.mqtt.broker.session;

import com.mqtt.broker.session.persistence.strategy.SessionPersistenceStrategy;
import com.mqtt.broker.repository.SubscriptionRepository;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<SocketChannel, Session> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, Session> persistentSessions = new ConcurrentHashMap<>();

    private final SubscriptionRepository subscriptionRepository;
    private final SessionPersistenceStrategy persistenceStrategy;

    public SessionManager(SubscriptionRepository subscriptionRepository,
                          SessionPersistenceStrategy persistenceStrategy) {
        this.subscriptionRepository = subscriptionRepository;
        this.persistenceStrategy = persistenceStrategy;
        this.persistentSessions.putAll(persistenceStrategy.load());
    }

    public Session getSession(SocketChannel channel) {
        return activeSessions.get(channel);
    }

    public Collection<Session> getActiveSessions() {
        return activeSessions.values();
    }

    public void registerSession(SocketChannel channel, Session session) {
        activeSessions.put(channel, session);
    }

    public SocketChannel getClientChannel(String clientId) {
        return activeSessions.entrySet().stream()
                .filter(entry -> entry.getValue().getClientId().equals(clientId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public Session getPersistentSession(String clientId) {
        return persistentSessions.get(clientId);
    }

    public Session removePersistentSession(String clientId) {
        return persistentSessions.remove(clientId);
    }

    public void closeSession(SocketChannel channel) {
        Session session = activeSessions.remove(channel);
        if (session == null) {
            return;
        }
        if (session.isCleanSession()) {
            subscriptionRepository.removeForClient(session.getClientId());
        } else {
            persistentSessions.put(session.getClientId(), session);
        }
    }

    public void persistSessions() {
        activeSessions.values().stream()
                .filter(s -> !s.isCleanSession())
                .forEach(s -> persistentSessions.put(s.getClientId(), s));

        persistenceStrategy.save(persistentSessions.values());
    }
}
