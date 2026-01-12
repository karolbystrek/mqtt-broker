package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.PINGRESP;

public record PingRespPacket(MqttFixedHeader fixedHeader) implements MqttPacket {
    public PingRespPacket {
        if (fixedHeader.packetType() != PINGRESP) {
            throw invalidPacketType(PingRespPacket.class);
        }
    }
}
