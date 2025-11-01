package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.ConnAckPacket.ConnAckVariableHeader;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static com.mqtt.broker.packet.MqttControlPacketType.CONNACK;

@RequiredArgsConstructor
public class ConnectPacketHandler implements MqttPacketHandler {

    private static final int MQTT_3_1_1_VERSION = 4;
    private static final String MQTT_PROTOCOL_NAME = "MQTT";

    private final MqttPacketEncoder encoder;
    private final Map<String, Session> sessions;

    @Override
    public void handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof ConnectPacket connectPacket)) {
            return;
        }
        System.out.println("Received CONNECT packet from client: " + clientChannel.getRemoteAddress());

        var variableHeader = connectPacket.getVariableHeader();

        if (!variableHeader.protocolName().equals(MQTT_PROTOCOL_NAME) || variableHeader.protocolVersion() != MQTT_3_1_1_VERSION) {
            sendConnAck(clientChannel, (byte) 0, 1); // Connection Refused, unacceptable protocol version
            System.out.println("Connection refused for " + clientChannel.getRemoteAddress() + ": Unsupported protocol");
            clientChannel.close();
            return;
        }

        String clientId = connectPacket.getPayload().clientId();
        boolean cleanSessionFlag = variableHeader.cleanSession();
        byte sessionPresentFlag = 0;
        if (cleanSessionFlag) {
            sessions.remove(clientId);
        } else if (sessions.containsKey(clientId)) {
            sessionPresentFlag = 1;
        }

        var session = new Session(clientId, cleanSessionFlag);
        sessions.put(clientId, session);

        System.out.println("Client connected with Client ID: " + clientId);

        sendConnAck(clientChannel, sessionPresentFlag, 0);
    }

    private void sendConnAck(SocketChannel clientChannel, byte sessionPresent, int returnCode) throws IOException {
        var connAckHeader = new ConnAckVariableHeader(sessionPresent, returnCode);
        var connAckPacket = new ConnAckPacket(new MqttFixedHeader(CONNACK, (byte) 0, 2), connAckHeader);

        var responseBuffer = encoder.encode(connAckPacket);
        responseBuffer.flip();
        while (responseBuffer.hasRemaining()) {
            clientChannel.write(responseBuffer);
        }
    }
}
