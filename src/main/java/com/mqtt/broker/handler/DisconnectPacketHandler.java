package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.trie.TopicTree;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static com.mqtt.broker.handler.HandlerResult.empty;

@RequiredArgsConstructor
public final class DisconnectPacketHandler implements MqttPacketHandler {

    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, Session> persistentSessions;
    private final TopicTree topicTree;
    private final Map<String, SocketChannel> clientIdToChannel;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof DisconnectPacket disconnectPacket)) {
            return empty();
        }

        System.out.println("Received DISCONNECT packet: " + disconnectPacket);

        Session session = activeSessions.get(clientChannel);
        if (session == null) {
            System.err.println("No active session found for disconnecting client");
            return empty();
        }

        // TODO: Discard any Will Message associated with the connection (MQTT-3.14.4-3)

        String clientId = session.getClientId();
        if (session.isCleanSession()) {
            topicTree.removeAllSubscriptionsFor(clientId);
        } else {
            persistentSessions.put(clientId, session);
            System.out.println("Saved persistent session for client: " + clientId);
        }

        activeSessions.remove(clientChannel);
        clientIdToChannel.remove(clientId);

        return empty();
    }
}
