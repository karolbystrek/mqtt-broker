package com.mqtt.broker.packet;

import static com.mqtt.broker.packet.MqttPacketType.CONNECT;

public record ConnectPacket(
        MqttFixedHeader fixedHeader,
        ConnectVariableHeader variableHeader,
        ConnectPayload payload
) implements MqttPacket {

    public ConnectPacket {
        if (fixedHeader.packetType() != CONNECT) {
            throw new IllegalArgumentException("Invalid packet type for ConnectPacket: " + fixedHeader.packetType());
        }
    }

    public record ConnectVariableHeader(
            String protocolName,
            int protocolVersion,
            boolean cleanSession,
            boolean willFlag,
            int willQos,
            boolean willRetain,
            boolean hasPassword,
            boolean hasUsername,
            int keepAlive
    ) {
    }

    public record ConnectPayload(
            String clientId,
            String willTopic,    // Optional
            String willMessage,  // Optional
            String username,     // Optional
            String password      // Optional
    ) {
    }
}
