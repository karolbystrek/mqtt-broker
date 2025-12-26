package com.mqtt.broker.handler.strategy;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.Session;
import com.mqtt.broker.Session.WillMessage;
import com.mqtt.broker.event.ClientConnectedEvent;
import com.mqtt.broker.event.CloseConnectionEvent;
import com.mqtt.broker.handler.HandlerResult;
import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.ConnAckPacket.ConnAckVariableHeader;
import com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.ConnectPacket.ConnectVariableHeader;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.trie.visitor.SubscriptionCleanupVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import static com.mqtt.broker.handler.HandlerResult.withEvent;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndEvent;
import static com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode.CONNECTION_ACCEPTED;
import static com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
import static com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
import static com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;
import static com.mqtt.broker.packet.MqttPacketType.CONNACK;

@RequiredArgsConstructor
@Slf4j
public class ConnectPacketHandlerStrategy implements PacketHandlerStrategy<ConnectPacket> {

    private static final int PROTOCOL_VERSION = 4; // 3.1.1 protocol version
    private static final String PROTOCOL_NAME = "MQTT";

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, ConnectPacket packet) throws IOException {
        if (context.getSession(clientChannel) != null) {
            log.error("Protocol violation: Second CONNECT packet received from already connected client. Disconnecting.");
            return withEvent(new CloseConnectionEvent(clientChannel));
        }

        var validationResult = validateConnection(packet, clientChannel);
        if (validationResult.isPresent()) {
            return validationResult.get();
        }

        String clientId = packet.getPayload().clientId();
        String username = packet.getPayload().username();
        var variableHeader = packet.getVariableHeader();

        // Handle existing connection with same Client ID
        SocketChannel existingClientChannel = context.getClientChannel(clientId);
        if (existingClientChannel != null && existingClientChannel != clientChannel) {
            log.info("Client with ID {} already connected. Disconnecting old connection.", clientId);
            existingClientChannel.close();
        }

        var session = resolveSession(clientId, username, variableHeader.cleanSession(), variableHeader.keepAlive());
        byte sessionPresentFlag;

        // If CleanSession is set to 1, the Client and Server MUST discard any previous Session and start a new one.
        if (variableHeader.cleanSession()) {
            sessionPresentFlag = 0;
        } else {
            // If CleanSession is set to 0, the Server MUST resume communications with the Client based on state from the current Session (as identified by the Client identifier).
            if (context.getPersistentSession(clientId) != null) {
                sessionPresentFlag = 1;
            } else {
                sessionPresentFlag = 0;
            }
        }

        if (variableHeader.willFlag()) {
            session.setWillMessage(new WillMessage(
                    packet.getPayload().willTopic(),
                    packet.getPayload().willMessage(),
                    variableHeader.willRetain(),
                    variableHeader.willQos()
            ));
        }

        session.updateLastActivity();
        context.registerSession(clientChannel, session);

        var connAckPacket = createConnAckPacket(sessionPresentFlag, CONNECTION_ACCEPTED);

        return withResponseAndEvent(connAckPacket, new ClientConnectedEvent(clientChannel, session));
    }

    private Optional<HandlerResult> validateConnection(ConnectPacket packet, SocketChannel clientChannel) throws IOException {
        var variableHeader = packet.getVariableHeader();

        if (!isProtocolValid(variableHeader)) {
            log.warn("Connection refused for {}: Unsupported protocol", clientChannel.getRemoteAddress());
            return Optional.of(withResponseAndEvent(
                    createConnAckPacket(0, CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION),
                    new CloseConnectionEvent(clientChannel)
            ));
        }

        if (!areConnectFlagsValid(variableHeader)) {
            log.warn("Connection refused for {}: Invalid connect flags", clientChannel.getRemoteAddress());
            return Optional.of(withEvent(new CloseConnectionEvent(clientChannel)));
        }

        String clientId = packet.getPayload().clientId();
        if (!isClientIdValid(clientId)) {
            log.warn("Connection refused for {}: Identifier rejected", clientChannel.getRemoteAddress());
            return Optional.of(withResponseAndEvent(
                    createConnAckPacket(0, CONNECTION_REFUSED_IDENTIFIER_REJECTED),
                    new CloseConnectionEvent(clientChannel)
            ));
        }

        var username = packet.getPayload().username();
        var password = packet.getPayload().password();
        if (!context.getAuthorizationService().authenticate(username, password)) {
            log.warn("Connection refused for {}: Bad user name or password", clientChannel.getRemoteAddress());
            return Optional.of(withResponseAndEvent(
                    createConnAckPacket(0, CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD),
                    new CloseConnectionEvent(clientChannel)
            ));
        }

        return Optional.empty();
    }

    private Session resolveSession(String clientId, String username, boolean cleanSession, int keepAlive) {
        if (cleanSession) {
            Session oldPersistentSession = context.removePersistentSession(clientId);
            if (oldPersistentSession != null) {
                var visitor = new SubscriptionCleanupVisitor(clientId);
                context.getSubscriptionTree().accept(visitor);
                oldPersistentSession.clearPendingMessages();
            }
            return new Session(clientId, username, true, keepAlive);
        } else {
            // Persistent session: restore if exists, otherwise create new
            Session session = context.removePersistentSession(clientId);
            if (session != null) {
                session.updateKeepAlive(keepAlive);
            } else {
                session = new Session(clientId, username, false, keepAlive);
            }
            return session;
        }
    }

    private boolean isProtocolValid(ConnectVariableHeader variableHeader) {
        return PROTOCOL_NAME.equals(variableHeader.protocolName()) && variableHeader.protocolVersion() == PROTOCOL_VERSION;
    }

    private boolean areConnectFlagsValid(ConnectVariableHeader variableHeader) {
        boolean willFlagValid = variableHeader.willFlag() || (variableHeader.willQos() == 0 && !variableHeader.willRetain());
        boolean willQosValid = variableHeader.willQos() >= 0 && variableHeader.willQos() <= 2;
        boolean usernameFlagValid = !variableHeader.hasPassword() || variableHeader.hasUsername();

        return willFlagValid && willQosValid && usernameFlagValid;
    }

    private boolean isClientIdValid(String clientId) {
        return clientId != null && !clientId.isEmpty() && clientId.length() <= 23 && clientId.matches("[0-9a-zA-Z]+");
    }

    private ConnAckPacket createConnAckPacket(int sessionPresent, MqttConnectReturnCode returnCode) {
        var connAckHeader = new ConnAckVariableHeader((byte) sessionPresent, returnCode.getCode());
        return new ConnAckPacket(new MqttFixedHeader(CONNACK, (byte) 0, 2), connAckHeader);
    }

}
