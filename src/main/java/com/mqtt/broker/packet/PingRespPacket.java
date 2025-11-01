package com.mqtt.broker.packet;

import static com.mqtt.broker.packet.MqttControlPacketType.PINGRESP;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

public final class PingRespPacket extends MqttPacket {

    public PingRespPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGRESP) {
            throw invalidPacketType(PingRespPacket.class);
        }
    }
}
