package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttControlPacketType;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.ConnAckPacket.ConnAckVariableHeader;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@RequiredArgsConstructor
public class ConnectPacketHandler implements MqttPacketHandler {

    private static final int MQTT_3_1_1_VERSION = 4;
    private static final String MQTT_PROTOCOL_NAME = "MQTT";

    private final MqttPacketEncoder encoder;

    @Override
    public void handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof ConnectPacket connectPacket)) {
            return;
        }
        System.out.println("Received CONNECT packet from client: " + clientChannel.getRemoteAddress());

        var variableHeader = connectPacket.getVariableHeader();

        if (!variableHeader.protocolName().equals(MQTT_PROTOCOL_NAME) || variableHeader.protocolVersion() != MQTT_3_1_1_VERSION) {
            sendConnAck(clientChannel, (byte) 0x01); // Connection Refused, unacceptable protocol version
            System.out.println("Connection refused for " + clientChannel.getRemoteAddress() + ": Unsupported protocol");
            clientChannel.close();
            return;
        }

        String clientId = connectPacket.getPayload().clientId();
        System.out.println("Client connected with Client ID: " + clientId);

        sendConnAck(clientChannel, (byte) 0x00); // Connection Accepted
    }

    private void sendConnAck(SocketChannel clientChannel, byte returnCode) throws IOException {
        var connAckHeader = new ConnAckVariableHeader((byte) 0, returnCode); // Session Present flag is 0 for now.
        var connAckPacket = new ConnAckPacket(new MqttFixedHeader(MqttControlPacketType.CONNACK, (byte) 0, 2), connAckHeader);

        var responseBuffer = encoder.encode(connAckPacket);
        responseBuffer.flip();
        while (responseBuffer.hasRemaining()) {
            clientChannel.write(responseBuffer);
        }
    }
}
