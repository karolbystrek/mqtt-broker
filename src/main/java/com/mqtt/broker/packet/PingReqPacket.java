package com.mqtt.broker.packet;

import com.mqtt.broker.MqttFixedHeader;

import static com.mqtt.broker.MqttControlPacketType.PINGREQ;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

public final class PingReqPacket extends MqttPacket {

    public PingReqPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGREQ) {
            throw invalidPacketType(PingReqPacket.class);
        }
    }
}
