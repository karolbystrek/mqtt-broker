package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.Session.WillMessage;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.ConnAckPacket.ConnAckVariableHeader;
import com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.ConnectPacket.ConnectVariableHeader;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import com.mqtt.broker.event.ClientConnectedEvent;
import com.mqtt.broker.event.CloseConnectionEvent;

import static com.mqtt.broker.handler.HandlerResult.withResponseAndEvent;
import static com.mqtt.broker.handler.HandlerResult.withEvent;
import static com.mqtt.broker.packet.MqttControlPacketType.CONNACK;
import static com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode.CONNECTION_ACCEPTED;
import static com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD;
import static com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED;
import static com.mqtt.broker.packet.ConnAckPacket.MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION;

@RequiredArgsConstructor
@Slf4j
public class ConnectPacketHandler implements MqttPacketHandler {

    private static final int PROTOCOL_VERSION = 4; // 3.1.1 protocol version
    private static final String PROTOCOL_NAME = "MQTT";

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        ConnectPacket connectPacket = (ConnectPacket) packet;

        if (context.getSession(clientChannel) != null) {
            log.error("Protocol violation: Second CONNECT packet received from already connected client. Disconnecting.");
            return withEvent(new CloseConnectionEvent(clientChannel));
        }

        log.info("Received CONNECT packet: {}", connectPacket);

        var validationResult = validateConnection(connectPacket, clientChannel);
        if (validationResult.isPresent()) {
            return validationResult.get();
        }

        String clientId = connectPacket.getPayload().clientId();
        var variableHeader = connectPacket.getVariableHeader();
        
        // Handle existing connection with same Client ID
        SocketChannel existingClientChannel = context.getClientChannel(clientId);
        if (existingClientChannel != null && existingClientChannel != clientChannel) {
            log.info("Client with ID {} already connected. Disconnecting old connection.", clientId);
            existingClientChannel.close();
        }

        Session session = resolveSession(clientId, variableHeader.cleanSession(), variableHeader.keepAlive());
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
                    connectPacket.getPayload().willTopic(),
                    connectPacket.getPayload().willMessage(),
                    variableHeader.willRetain(),
                    variableHeader.willQos()
            ));
        }

        session.updateLastActivity();
        context.registerSession(clientChannel, session);

        var connAckPacket = createConnAckPacket(sessionPresentFlag, CONNECTION_ACCEPTED);
        
        return withResponseAndEvent(connAckPacket, new ClientConnectedEvent(clientChannel, session));
    }

    private Optional<HandlerResult> validateConnection(ConnectPacket connectPacket, SocketChannel clientChannel) throws IOException {
        var variableHeader = connectPacket.getVariableHeader();

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

        String clientId = connectPacket.getPayload().clientId();
        if (!isClientIdValid(clientId)) {
            log.warn("Connection refused for {}: Identifier rejected", clientChannel.getRemoteAddress());
            return Optional.of(withResponseAndEvent(
                    createConnAckPacket(0, CONNECTION_REFUSED_IDENTIFIER_REJECTED),
                    new CloseConnectionEvent(clientChannel)
            ));
        }
        
        if (variableHeader.hasUsername()) {
             String username = connectPacket.getPayload().username();
             String password = connectPacket.getPayload().password();
             if (!context.getUserRegistry().validate(username, password)) {
                 log.warn("Connection refused for {}: Bad user name or password", clientChannel.getRemoteAddress());
                 return Optional.of(withResponseAndEvent(
                         createConnAckPacket(0, CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD),
                         new CloseConnectionEvent(clientChannel)
                 ));
             }
        }
        
        return Optional.empty();
    }

    private Session resolveSession(String clientId, boolean cleanSession, int keepAlive) {
        if (cleanSession) {
            Session oldPersistentSession = context.removePersistentSession(clientId);
            if (oldPersistentSession != null) {
                context.getTopicTree().removeAllSubscriptionsFor(clientId);
                oldPersistentSession.clearPendingMessages();
            }
            return new Session(clientId, true, keepAlive);
        } else {
            // Persistent session: restore if exists, otherwise create new
            Session session = context.removePersistentSession(clientId);
            if (session != null) {
                session.updateKeepAlive(keepAlive);
            } else {
                session = new Session(clientId, false, keepAlive);
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
