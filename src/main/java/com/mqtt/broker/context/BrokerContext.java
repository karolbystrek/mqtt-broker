package com.mqtt.broker.context;

import com.mqtt.broker.Session;
import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.service.PendingMessageDeliveryService;
import com.mqtt.broker.trie.TopicTree;
import lombok.Getter;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BrokerContext {

    private final BrokerConfiguration config;
    private final TopicTree topicTree;
    private final PendingMessageDeliveryService pendingMessageService;
    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, SocketChannel> clientIdToChannel;
    private final Map<String, Session> persistentSessions;

    public BrokerContext(BrokerConfiguration config, 
                         TopicTree topicTree, 
                         PendingMessageDeliveryService pendingMessageService) {
        this.config = config;
        this.topicTree = topicTree;
        this.pendingMessageService = pendingMessageService;
        
        this.activeSessions = new ConcurrentHashMap<>();
        this.clientIdToChannel = new ConcurrentHashMap<>();
        this.persistentSessions = new ConcurrentHashMap<>();
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

    public void savePersistentSession(String clientId, Session session) {
        persistentSessions.put(clientId, session);
    }
    
    public Session removePersistentSession(String clientId) {
        return persistentSessions.remove(clientId);
    }
}
