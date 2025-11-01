package com.mqtt.broker.packet;

import static com.mqtt.broker.packet.MqttControlPacketType.PINGREQ;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

public final class PingReqPacket extends MqttPacket {

    public PingReqPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGREQ) {
            throw invalidPacketType(PingReqPacket.class);
        }
    }
}
