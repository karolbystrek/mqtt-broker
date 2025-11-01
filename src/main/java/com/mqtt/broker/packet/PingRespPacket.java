package com.mqtt.broker.packet;

import com.mqtt.broker.MqttFixedHeader;

import static com.mqtt.broker.MqttControlPacketType.PINGRESP;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

public final class PingRespPacket extends MqttPacket {

    public PingRespPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGRESP) {
            throw invalidPacketType(PingRespPacket.class);
        }
    }
}
