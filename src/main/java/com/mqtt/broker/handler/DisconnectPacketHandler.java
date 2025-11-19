package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.trie.TopicTree;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withAction;

@RequiredArgsConstructor
@Slf4j
public final class DisconnectPacketHandler implements MqttPacketHandler {

    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, Session> persistentSessions;
    private final TopicTree topicTree;
    private final Map<String, SocketChannel> clientIdToChannel;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        var disconnectPacket = (DisconnectPacket) packet;

        log.info("Received DISCONNECT packet: {}", disconnectPacket);

        Session session = activeSessions.get(clientChannel);
        if (session == null) {
            log.warn("No active session found for disconnecting client");
            return withAction(java.nio.channels.SocketChannel::close);
        }

        String clientId = session.getClientId();
        if (session.isCleanSession()) {
            topicTree.removeAllSubscriptionsFor(clientId);
        } else {
            persistentSessions.put(clientId, session);
            log.info("Saved persistent session for client: {}", clientId);
        }

        activeSessions.remove(clientChannel);
        clientIdToChannel.remove(clientId);

        return empty();
    }
}
