package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.PINGRESP;

public final class PingRespPacket extends MqttPacket {

    public PingRespPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGRESP) {
            throw invalidPacketType(PingRespPacket.class);
        }
    }
}
