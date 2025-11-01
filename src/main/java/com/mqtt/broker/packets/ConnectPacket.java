package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import static com.mqtt.broker.MqttControlPacketType.CONNECT;

@Getter
public final class ConnectPacket extends MqttPacket {

    private final ConnectVariableHeader variableHeader;
    private final ConnectPayload payload;

    ConnectPacket(MqttFixedHeader fixedHeader, ConnectVariableHeader variableHeader, ConnectPayload payload) {
        super(fixedHeader);
        if (fixedHeader.packetType() != CONNECT) {
            throw new IllegalArgumentException("Invalid packet type for ConnectPacket: " + fixedHeader.packetType());
        }
        this.variableHeader = variableHeader;
        this.payload = payload;
    }

    public record ConnectVariableHeader(
            String protocolName,
            int protocolLevel,
            boolean cleanSession,
            boolean willFlag,
            int willQos,
            boolean willRetain,
            boolean passwordFlag,
            boolean usernameFlag,
            int keepAlive
    ) {
    }

    public record ConnectPayload(
            String clientId,
            String willTopic,    // Optional, non-null only if willFlag is true
            String willMessage,  // Optional, non-null only if willFlag is true
            String username,     // Optional, non-null only if hasUsername is true
            String password      // Optional, non-null only if hasPassword is true
    ) {
    }
}
