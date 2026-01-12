package com.mqtt.broker;

import com.mqtt.broker.auth.AuthorizationService;
import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.persistence.SessionPersistenceService;
import com.mqtt.broker.service.MessageDeliveryService;
import com.mqtt.broker.trie.RetainedMessage;
import com.mqtt.broker.trie.TopicTree;
import com.mqtt.broker.trie.strategy.SubscriptionPruningStrategy;
import lombok.Getter;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BrokerContext {

    private final Map<String, SocketChannel> clientIdToChannel = new ConcurrentHashMap<>();
    private final Map<SocketChannel, Session> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, Session> persistentSessions = new ConcurrentHashMap<>();
    private final TopicTree<Set<String>> subscriptionTree = new TopicTree<>();
    private final TopicTree<RetainedMessage> retainedMessageTree = new TopicTree<>();

    private final BrokerConfiguration config;
    private final AuthorizationService authorizationService;
    private final MessageDeliveryService messageDeliveryService;
    private final SessionPersistenceService sessionPersistenceService;

    public BrokerContext(BrokerConfiguration config) {
        this.config = config;
        this.authorizationService = new AuthorizationService(config);
        this.messageDeliveryService = new MessageDeliveryService(this);
        this.sessionPersistenceService = new SessionPersistenceService();
        if (!config.getServer().isCleanSession()) {
            this.persistentSessions.putAll(sessionPersistenceService.load());
        }
    }

    public Session getSession(SocketChannel channel) {
        return activeSessions.get(channel);
    }

    public void registerSession(SocketChannel channel, Session session) {
        activeSessions.put(channel, session);
        clientIdToChannel.put(session.getClientId(), channel);
    }

    public SocketChannel getClientChannel(String clientId) {
        return clientIdToChannel.get(clientId);
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
        clientIdToChannel.remove(session.getClientId());
        if (session.isCleanSession()) {
            var strategy = new SubscriptionPruningStrategy(session.getClientId());
            subscriptionTree.perform(strategy);
        } else {
            persistentSessions.put(session.getClientId(), session);
        }
    }

    public void persistSessions() {
        if (!config.getServer().isCleanSession()) {
            activeSessions.values().stream()
                    .filter(s -> !s.isCleanSession())
                    .forEach(s -> persistentSessions.put(s.getClientId(), s));

            sessionPersistenceService.save(persistentSessions.values());
        }
    }
}
