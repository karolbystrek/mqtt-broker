package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.trie.TopicTree;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;

@RequiredArgsConstructor
public final class DisconnectPacketHandler implements MqttPacketHandler {

    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, Session> persistentSessions;
    private final TopicTree topicTree;
    private final Map<String, SocketChannel> clientIdToChannel;

    @Override
    public Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof DisconnectPacket)) {
            return empty();
        }

        System.out.println("Received DISCONNECT packet from: " + clientChannel.getRemoteAddress());

        Session session = activeSessions.get(clientChannel);
        if (session == null) {
            System.err.println("No active session found for disconnecting client");
            clientChannel.close();
            return empty();
        }

        // TODO: Discard any Will Message associated with the connection (MQTT-3.14.4-3)

        if (session.isCleanSession()) {
            topicTree.removeAllSubscriptionsFor(session.getClientId());
        } else {
            persistentSessions.put(session.getClientId(), session);
        }

        activeSessions.remove(clientChannel);
        clientIdToChannel.remove(session.getClientId());

        clientChannel.close();
        System.out.println("Client disconnected: " + session.getClientId());

        return empty();
    }
}
