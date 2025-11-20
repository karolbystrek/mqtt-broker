package com.mqtt.broker.packet;

import lombok.Getter;
import lombok.ToString;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.CONNACK;

@Getter
@ToString
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

    public enum MqttConnectReturnCode {
        CONNECTION_ACCEPTED(0x00),
        CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION(0x01),
        CONNECTION_REFUSED_IDENTIFIER_REJECTED(0x02),
        CONNECTION_REFUSED_SERVER_UNAVAILABLE(0x03),
        CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD(0x04),
        CONNECTION_REFUSED_NOT_AUTHORIZED(0x05);

        private final int code;

        MqttConnectReturnCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
