package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.ConnectPacket;

import static com.mqtt.broker.decoder.DecoderUtils.decodeString;
import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;
import static com.mqtt.broker.packet.ConnectPacket.ConnectPayload;
import static com.mqtt.broker.packet.ConnectPacket.ConnectVariableHeader;

class ConnectPacketDecoder implements PacketDecoder<ConnectPacket> {

    private static final int PROTOCOL_VERSION_MASK = 0xFF;
    private static final int USERNAME_FLAG_MASK = 0b1000_0000;
    private static final int PASSWORD_FLAG_MASK = 0b0100_0000;
    private static final int WILL_RETAIN_MASK = 0b0010_0000;
    private static final int WILL_QOS_MASK = 0b0001_1000;
    private static final int WILL_QOS_SHIFT = 3;
    private static final int WILL_FLAG_MASK = 0b0000_0100;
    private static final int CLEAN_SESSION_MASK = 0b0000_0010;

    @Override
    public ConnectPacket decode(MqttFrame frame) {
        var fixedHeader = frame.fixedHeader();
        var body = frame.body();

        String protocolName = decodeString(body);
        int protocolVersion = body.get() & PROTOCOL_VERSION_MASK;

        byte connectFlagsByte = body.get();
        boolean hasUsername = (connectFlagsByte & USERNAME_FLAG_MASK) != 0;
        boolean hasPassword = (connectFlagsByte & PASSWORD_FLAG_MASK) != 0;
        if (!hasUsername && hasPassword) {
            throw new IllegalArgumentException("Password flag set without username flag");
        }
        boolean willRetain = (connectFlagsByte & WILL_RETAIN_MASK) != 0;
        int willQos = (connectFlagsByte & WILL_QOS_MASK) >> WILL_QOS_SHIFT;
        boolean willFlag = (connectFlagsByte & WILL_FLAG_MASK) != 0;
        boolean cleanSession = (connectFlagsByte & CLEAN_SESSION_MASK) != 0;

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
