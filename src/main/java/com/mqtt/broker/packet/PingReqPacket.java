package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.PINGREQ;

public record PingReqPacket(MqttFixedHeader fixedHeader) implements MqttPacket {
    public PingReqPacket {
        if (fixedHeader.packetType() != PINGREQ) {
            throw invalidPacketType(PingReqPacket.class);
        }
    }
}
