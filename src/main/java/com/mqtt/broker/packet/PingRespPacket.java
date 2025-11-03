package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGRESP;

public final class PingRespPacket extends MqttPacket {

    public PingRespPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGRESP) {
            throw invalidPacketType(PingRespPacket.class);
        }
    }

    @Override
    public String toString() {
        return "PingRespPacket{" +
                "fixedHeader=" + getFixedHeader() +
                '}';
    }
}
