package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.event.ClientConnectedEvent;
import com.mqtt.broker.event.CloseConnectionEvent;
import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.ConnAckPacket.ConnAckVariableHeader;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.ConnectPacket.ConnectVariableHeader;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.pipeline.ProcessingResult;
import com.mqtt.broker.session.Session;
import com.mqtt.broker.session.Session.WillMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import static com.mqtt.broker.packet.MqttPacketType.CONNACK;
import static com.mqtt.broker.pipeline.ProcessingResult.withEvent;
import static com.mqtt.broker.pipeline.ProcessingResult.withResponseAndEvent;

@RequiredArgsConstructor
@Slf4j
class ConnectPacketHandler implements PacketHandler<ConnectPacket> {

    private static final int PROTOCOL_VERSION = 4; // 3.1.1 protocol version
    private static final String PROTOCOL_NAME = "MQTT";

    private static final String CLIENT_ID_FORMAT = "[0-9a-zA-Z]+";

    private static final int CONNECTION_ACCEPTED = 0x00;
    private static final int CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION = 0x01;
    private static final int CONNECTION_REFUSED_IDENTIFIER_REJECTED = 0x02;
    private static final int CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD = 0x04;

    private static final byte SESSION_NOT_PRESENT = 0x00;
    private static final byte SESSION_PRESENT = 0x01;

    private final BrokerContext context;

    @Override
    public ProcessingResult handle(SocketChannel clientChannel, ConnectPacket packet) throws IOException {
        if (context.getSessionManager().getSession(clientChannel) != null) {
            log.error("Protocol violation: Second CONNECT packet received from already connected client. Disconnecting.");
            return withEvent(new CloseConnectionEvent(clientChannel));
        }

        var validationResult = validateConnection(packet, clientChannel);
        if (validationResult.isPresent()) {
            return validationResult.get();
        }

        String clientId = packet.payload().clientId();
        String username = packet.payload().username();
        var variableHeader = packet.variableHeader();

        // Handle existing connection with same Client ID
        SocketChannel existingClientChannel = context.getSessionManager().getClientChannel(clientId);
        if (existingClientChannel != null && existingClientChannel != clientChannel) {
            log.info("Client with ID {} already connected. Disconnecting old connection.", clientId);
            existingClientChannel.close();
        }

        var session = resolveSession(clientId, username, variableHeader.cleanSession(), variableHeader.keepAlive());

        byte sessionPresentFlag = isSessionPresentFlag(packet);

        if (variableHeader.willFlag()) {
            session.setWillMessage(new WillMessage(
                    packet.payload().willTopic(),
                    packet.payload().willMessage(),
                    variableHeader.willRetain(),
                    variableHeader.willQos()
            ));
        }

        session.updateLastActivity();
        context.getSessionManager().registerSession(clientChannel, session);

        var connAckPacket = createConnAckPacket(sessionPresentFlag, CONNECTION_ACCEPTED);

        return withResponseAndEvent(connAckPacket, new ClientConnectedEvent(clientChannel, session));
    }

    private Optional<ProcessingResult> validateConnection(ConnectPacket packet, SocketChannel clientChannel) throws IOException {
        var variableHeader = packet.variableHeader();

        if (!isProtocolValid(variableHeader)) {
            log.warn("Connection refused for {}: Unsupported protocol", clientChannel.getRemoteAddress());
            return Optional.of(withResponseAndEvent(
                    createConnAckPacket(SESSION_NOT_PRESENT, CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION),
                    new CloseConnectionEvent(clientChannel)
            ));
        }

        if (!areConnectFlagsValid(variableHeader)) {
            log.warn("Connection refused for {}: Invalid connect flags", clientChannel.getRemoteAddress());
            return Optional.of(withEvent(new CloseConnectionEvent(clientChannel)));
        }

        String clientId = packet.payload().clientId();
        if (!isClientIdValid(clientId)) {
            log.warn("Connection refused for {}: Identifier rejected", clientChannel.getRemoteAddress());
            return Optional.of(withResponseAndEvent(
                    createConnAckPacket(SESSION_NOT_PRESENT, CONNECTION_REFUSED_IDENTIFIER_REJECTED),
                    new CloseConnectionEvent(clientChannel)
            ));
        }

        if (!context.getAuthorizationService().authenticate(packet)) {
            log.warn("Connection refused for {}: Bad user name or password", clientChannel.getRemoteAddress());
            return Optional.of(withResponseAndEvent(
                    createConnAckPacket(SESSION_NOT_PRESENT, CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD),
                    new CloseConnectionEvent(clientChannel)
            ));
        }

        return Optional.empty();
    }

    private Session resolveSession(String clientId, String username, boolean cleanSession, int keepAlive) {
        if (cleanSession) {
            Session oldPersistentSession = context.getSessionManager().removePersistentSession(clientId);
            if (oldPersistentSession != null) {
                context.getSubscriptionRepository().removeForClient(clientId);
                oldPersistentSession.clearPendingMessages();
            }
            return new Session(clientId, username, true, keepAlive);
        } else {
            // Persistent session: restore if exists, otherwise create new
            Session session = context.getSessionManager().removePersistentSession(clientId);
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
        return clientId != null && !clientId.isEmpty() && clientId.length() <= 23 && clientId.matches(CLIENT_ID_FORMAT);
    }

    private byte isSessionPresentFlag(ConnectPacket packet) {
        if (!packet.variableHeader().cleanSession() &&
                context.getSessionManager().getPersistentSession(packet.payload().clientId()) != null) {
            return SESSION_PRESENT;
        }
        return SESSION_NOT_PRESENT;
    }

    private ConnAckPacket createConnAckPacket(byte connectAcknowledgeFlags, int returnCode) {
        var connAckHeader = new ConnAckVariableHeader(connectAcknowledgeFlags, returnCode);
        return new ConnAckPacket(new MqttFixedHeader(CONNACK, (byte) 0, 2), connAckHeader);
    }
}
