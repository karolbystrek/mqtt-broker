package com.mqtt.broker.context;

import com.mqtt.broker.Session;
import com.mqtt.broker.auth.UserRegistry;
import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.service.MessageDispatcher;
import com.mqtt.broker.service.MqttPacketSender;
import com.mqtt.broker.service.PendingMessageDeliveryService;
import com.mqtt.broker.service.SessionPersistenceService;
import com.mqtt.broker.trie.TopicTree;
import lombok.Getter;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BrokerContext {

    private final BrokerConfiguration config;
    private final UserRegistry userRegistry;
    private final TopicTree topicTree;
    private final MqttPacketSender packetSender;
    private final MessageDispatcher messageDispatcher;
    private final PendingMessageDeliveryService pendingMessageService;
    private final Map<String, SocketChannel> clientIdToChannel;
    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, Session> persistentSessions;
    private final SessionPersistenceService sessionPersistenceService;

    public BrokerContext(BrokerConfiguration config) {
        this.config = config;
        this.userRegistry = new UserRegistry();
        this.topicTree = new TopicTree();
        this.packetSender = new MqttPacketSender(new MqttPacketEncoder());
        this.messageDispatcher = new MessageDispatcher(this, packetSender);
        this.pendingMessageService = new PendingMessageDeliveryService(packetSender);
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

    public void removeSession(SocketChannel channel) {
        Session session = activeSessions.remove(channel);
        if (session != null) {
            clientIdToChannel.remove(session.getClientId());
        }
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

    public void closeSession(Session session) {
        if (session.isCleanSession()) {
            topicTree.removeAllSubscriptionsFor(session.getClientId());
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
