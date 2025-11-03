package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGREQ;

public final class PingReqPacket extends MqttPacket {

    public PingReqPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGREQ) {
            throw invalidPacketType(PingReqPacket.class);
        }
    }

    @Override
    public String toString() {
        return "PingReqPacket{" +
                "fixedHeader=" + getFixedHeader() +
                '}';
    }
}
