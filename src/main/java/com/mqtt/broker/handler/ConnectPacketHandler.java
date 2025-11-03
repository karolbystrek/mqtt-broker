package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.ConnAckPacket.ConnAckVariableHeader;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.service.PendingMessageDeliveryService;
import com.mqtt.broker.trie.TopicTree;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndAction;
import static com.mqtt.broker.packet.MqttControlPacketType.CONNACK;

@RequiredArgsConstructor
public class ConnectPacketHandler implements MqttPacketHandler {

    private static final int MQTT_3_1_1_VERSION = 4;
    private static final String MQTT_PROTOCOL_NAME = "MQTT";

    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, Session> persistentSessions;
    private final Map<String, SocketChannel> clientIdToChannel;
    private final TopicTree topicTree;
    private final PendingMessageDeliveryService pendingMessageService;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof ConnectPacket connectPacket)) {
            return empty();
        }

        // Check for protocol violation: multiple CONNECT packets from the same client
        if (activeSessions.containsKey(clientChannel)) {
            System.err.println("Protocol violation: Second CONNECT packet received from already connected client. Disconnecting.");
            clientChannel.close();
            return empty();
        }

        System.out.println("Received CONNECT packet: " + connectPacket);

        var variableHeader = connectPacket.getVariableHeader();

        if (!variableHeader.protocolName().equals(MQTT_PROTOCOL_NAME) || variableHeader.protocolVersion() != MQTT_3_1_1_VERSION) {
            System.out.println("Connection refused for " + clientChannel.getRemoteAddress() + ": Unsupported protocol");
            clientChannel.close();
            return withResponse(createConnAckPacket((byte) 0, 1)); // Connection Refused, unacceptable protocol version
        }

        String clientId = connectPacket.getPayload().clientId();
        boolean cleanSessionFlag = variableHeader.cleanSession();
        int keepAlive = variableHeader.keepAlive();
        byte sessionPresentFlag = 0;
        Session session;
        boolean hasPendingMessages = false;

        SocketChannel existingClientChannel = clientIdToChannel.get(clientId);
        if (existingClientChannel != null && existingClientChannel != clientChannel) {
            System.out.println("Client with ID " + clientId + " already connected. Disconnecting old connection.");
            existingClientChannel.close();
        }

        if (cleanSessionFlag) {
            Session oldPersistentSession = persistentSessions.remove(clientId);
            if (oldPersistentSession != null) {
                topicTree.removeAllSubscriptionsFor(clientId);
                oldPersistentSession.clearPendingMessages();
            }
            session = new Session(clientId, true, keepAlive);

        } else {
            // Persistent session: restore if exists, otherwise create new
            session = persistentSessions.remove(clientId);
            if (session != null) {
                sessionPresentFlag = 1;
                session.updateKeepAlive(keepAlive);
                hasPendingMessages = session.getPendingMessagesStream().findAny().isPresent();
            } else {
                session = new Session(clientId, false, keepAlive);
            }
        }

        session.updateLastActivity();
        activeSessions.put(clientChannel, session);
        clientIdToChannel.put(clientId, clientChannel);

        var connAckPacket = createConnAckPacket(sessionPresentFlag, 0);

        if (hasPendingMessages) {
            final Session sessionFinal = session;
            PostConnectionAction deliveryAction = channel -> pendingMessageService.deliverPendingMessages(channel, sessionFinal);
            return withResponseAndAction(connAckPacket, deliveryAction);
        }

        return withResponse(connAckPacket);
    }

    private ConnAckPacket createConnAckPacket(byte sessionPresent, int returnCode) {
        var connAckHeader = new ConnAckVariableHeader(sessionPresent, returnCode);
        return new ConnAckPacket(new MqttFixedHeader(CONNACK, (byte) 0, 2), connAckHeader);
    }
}
