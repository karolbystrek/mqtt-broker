package com.mqtt.broker;

import com.mqtt.broker.auth.AuthorizationService;
import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.event.BrokerEventListener;
import com.mqtt.broker.event.BrokerEventPublisher;
import com.mqtt.broker.persistence.SessionPersistenceService;
import com.mqtt.broker.service.MessageDeliveryService;
import com.mqtt.broker.trie.RetainedMessage;
import com.mqtt.broker.trie.TopicTree;
import com.mqtt.broker.trie.visitor.SubscriptionCleanupVisitor;
import lombok.Getter;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BrokerContext {

    private final BrokerConfiguration config;
    private final AuthorizationService authorizationService;
    private final BrokerEventPublisher eventPublisher;
    private final TopicTree<Set<String>> subscriptionTree;
    private final TopicTree<RetainedMessage> retainedMessageTree;
    private final MessageDeliveryService messageDeliveryService;
    private final Map<String, SocketChannel> clientIdToChannel;
    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, Session> persistentSessions;
    private final SessionPersistenceService sessionPersistenceService;

    public BrokerContext(BrokerConfiguration config) {
        this.config = config;
        this.authorizationService = new AuthorizationService(config);
        this.eventPublisher = new BrokerEventPublisher();
        this.eventPublisher.addListener(new BrokerEventListener(this));
        this.subscriptionTree = new TopicTree<>();
        this.retainedMessageTree = new TopicTree<>();
        this.messageDeliveryService = new MessageDeliveryService(this);
        this.clientIdToChannel = new ConcurrentHashMap<>();
        this.sessionPersistenceService = new SessionPersistenceService();
        this.activeSessions = new ConcurrentHashMap<>();
        if (config.getServer().isCleanSession()) {
            this.persistentSessions = new ConcurrentHashMap<>();
        } else {
            this.persistentSessions = new ConcurrentHashMap<>(sessionPersistenceService.load());
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
            var visitor = new SubscriptionCleanupVisitor(session.getClientId());
            subscriptionTree.accept(visitor);
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
