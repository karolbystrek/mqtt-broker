package com.mqtt.broker.packet;

import lombok.ToString;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGREQ;

@ToString
public final class PingReqPacket extends MqttPacket {

    public PingReqPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGREQ) {
            throw invalidPacketType(PingReqPacket.class);
        }
    }
}
