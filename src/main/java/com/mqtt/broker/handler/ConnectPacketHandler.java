package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.ConnAckPacket.ConnAckVariableHeader;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withAction;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndAction;
import static com.mqtt.broker.packet.MqttControlPacketType.CONNACK;

@RequiredArgsConstructor
@Slf4j
public class ConnectPacketHandler implements MqttPacketHandler {

    private static final int MQTT_3_1_1_VERSION = 4;
    private static final String MQTT_PROTOCOL_NAME = "MQTT";

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof ConnectPacket connectPacket)) {
            return empty();
        }

        // Check for protocol violation: multiple CONNECT packets from the same client
        if (context.getSession(clientChannel) != null) {
            log.error("Protocol violation: Second CONNECT packet received from already connected client. Disconnecting.");
            return withAction(java.nio.channels.SocketChannel::close);
        }

        log.info("Received CONNECT packet: {}", connectPacket);

        var variableHeader = connectPacket.getVariableHeader();

        if (!MQTT_PROTOCOL_NAME.equals(variableHeader.protocolName()) || variableHeader.protocolVersion() != MQTT_3_1_1_VERSION) {
            log.warn("Connection refused for {}: Unsupported protocol", clientChannel.getRemoteAddress());
            return HandlerResult.withResponseAndAction(
                    createConnAckPacket((byte) 0, 1),
                    java.nio.channels.SocketChannel::close
            );
        }

        String clientId = connectPacket.getPayload().clientId();
        boolean cleanSessionFlag = variableHeader.cleanSession();
        int keepAlive = variableHeader.keepAlive();
        byte sessionPresentFlag = 0;
        Session session;
        boolean hasPendingMessages = false;

        SocketChannel existingClientChannel = context.getClientChannel(clientId);
        if (existingClientChannel != null && existingClientChannel != clientChannel) {
            log.info("Client with ID {} already connected. Disconnecting old connection.", clientId);
            existingClientChannel.close();
        }

        if (cleanSessionFlag) {
            Session oldPersistentSession = context.removePersistentSession(clientId);
            if (oldPersistentSession != null) {
                context.getTopicTree().removeAllSubscriptionsFor(clientId);
                oldPersistentSession.clearPendingMessages();
            }
            session = new Session(clientId, true, keepAlive);

        } else {
            // Persistent session: restore if exists, otherwise create new
            session = context.removePersistentSession(clientId);
            if (session != null) {
                sessionPresentFlag = 1;
                session.updateKeepAlive(keepAlive);
                hasPendingMessages = session.getPendingMessagesStream().findAny().isPresent();
            } else {
                session = new Session(clientId, false, keepAlive);
            }
        }

        session.updateLastActivity();
        context.registerSession(clientChannel, session);

        var connAckPacket = createConnAckPacket(sessionPresentFlag, 0);

        if (hasPendingMessages) {
            final Session sessionFinal = session;
            PostConnectionAction deliveryAction = channel -> context.getPendingMessageService().deliverPendingMessages(channel, sessionFinal);
            return withResponseAndAction(connAckPacket, deliveryAction);
        }

        return withResponse(connAckPacket);
    }

    private ConnAckPacket createConnAckPacket(byte sessionPresent, int returnCode) {
        var connAckHeader = new ConnAckVariableHeader(sessionPresent, returnCode);
        return new ConnAckPacket(new MqttFixedHeader(CONNACK, (byte) 0, 2), connAckHeader);
    }
}
