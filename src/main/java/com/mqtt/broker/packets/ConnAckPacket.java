package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.CONNACK;

@Getter
public final class ConnAckPacket extends MqttPacket {

    private final ConnAckVariableHeader variableHeader;

    public ConnAckPacket(MqttFixedHeader fixedHeader, ConnAckVariableHeader variableHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != CONNACK) {
            throw invalidPacketType(ConnAckPacket.class);
        }
        this.variableHeader = variableHeader;
    }

    public record ConnAckVariableHeader(
            byte connectAcknowledgeFlags,
            int returnCode
    ) {

        public boolean isSessionPresent() {
            return (connectAcknowledgeFlags & 0x01) != 0;
        }
    }
}
