package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.ConnectPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.packet.ConnectPacket.ConnectPayload;
import static com.mqtt.broker.packet.ConnectPacket.ConnectVariableHeader;


public interface ConnectPacketDecoder extends MqttPacketDecoderInterface {

    default ConnectPacket decodeConnect(MqttFixedHeader fixedHeader, ByteBuffer body) {
        String protocolName = decodeString(body);
        int protocolVersion = body.get() & 0xFF;

        byte connectFlagsByte = body.get();
        boolean hasUsername = (connectFlagsByte & 0b1000_0000) != 0;
        boolean hasPassword = (connectFlagsByte & 0b0100_0000) != 0;
        boolean willRetain = (connectFlagsByte & 0b0010_0000) != 0;
        int willQos = (connectFlagsByte & 0b0001_1000) >> 3;
        boolean willFlag = (connectFlagsByte & 0b0000_0100) != 0;
        boolean cleanSession = (connectFlagsByte & 0b0000_0010) != 0;

        int keepAlive = decodeTwoByteInt(body);

        var variableHeader = new ConnectVariableHeader(protocolName, protocolVersion, cleanSession, willFlag, willQos, willRetain, hasPassword, hasUsername, keepAlive);

        String clientId = decodeString(body);
        String willTopic = willFlag ? decodeString(body) : null;
        String willMessage = willFlag ? decodeString(body) : null;
        String username = hasUsername ? decodeString(body) : null;
        String password = hasPassword ? decodeString(body) : null;

        var payload = new ConnectPayload(clientId, willTopic, willMessage, username, password);

        return new ConnectPacket(fixedHeader, variableHeader, payload);
    }
}
