package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.CONNACK;

public record ConnAckPacket(
        MqttFixedHeader fixedHeader,
        ConnAckVariableHeader variableHeader
) implements MqttPacket {

    public ConnAckPacket {
        if (fixedHeader.packetType() != CONNACK) {
            throw invalidPacketType(ConnAckPacket.class);
        }
    }

    public record ConnAckVariableHeader(
            byte connectAcknowledgeFlags,
            int returnCode
    ) {
        private static final int SESSION_PRESENT_MASK = 0x01;

        public boolean isSessionPresent() {
            return (connectAcknowledgeFlags & SESSION_PRESENT_MASK) != 0;
        }
    }
}
