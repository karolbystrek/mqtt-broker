package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.ConnAckPacket.ConnAckVariableHeader;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

import static com.mqtt.broker.packet.MqttControlPacketType.CONNACK;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@RequiredArgsConstructor
public class ConnectPacketHandler implements MqttPacketHandler {

    private static final int MQTT_3_1_1_VERSION = 4;
    private static final String MQTT_PROTOCOL_NAME = "MQTT";

    private final Map<SocketChannel, Session> activeSessions;
    private final Map<String, Session> persistentSessions;

    @Override
    public Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof ConnectPacket connectPacket)) {
            return empty();
        }

        System.out.println("Received CONNECT packet: " + connectPacket);

        var variableHeader = connectPacket.getVariableHeader();

        if (!variableHeader.protocolName().equals(MQTT_PROTOCOL_NAME) || variableHeader.protocolVersion() != MQTT_3_1_1_VERSION) {
            System.out.println("Connection refused for " + clientChannel.getRemoteAddress() + ": Unsupported protocol");
            clientChannel.close();
            return of(createConnAckPacket((byte) 0, 1)); // Connection Refused, unacceptable protocol version
        }

        String clientId = connectPacket.getPayload().clientId();
        boolean cleanSessionFlag = variableHeader.cleanSession();
        byte sessionPresentFlag = 0;
        Session session;

        if (cleanSessionFlag) {
            // Clean session: create new session and discard any persistent session
            persistentSessions.remove(clientId);
            session = new Session(clientId, true);
        } else {
            // Persistent session: restore if exists, otherwise create new
            session = persistentSessions.remove(clientId);
            if (session != null) {
                System.out.println("Restored persistent session for client: " + clientId);
                sessionPresentFlag = 1;
            } else {
                session = new Session(clientId, false);
            }
        }

        activeSessions.put(clientChannel, session);
        System.out.println("Client " + clientId + " connected");
        return of(createConnAckPacket(sessionPresentFlag, 0));
    }

    private ConnAckPacket createConnAckPacket(byte sessionPresent, int returnCode) {
        var connAckHeader = new ConnAckVariableHeader(sessionPresent, returnCode);
        return new ConnAckPacket(new MqttFixedHeader(CONNACK, (byte) 0, 2), connAckHeader);
    }
}
