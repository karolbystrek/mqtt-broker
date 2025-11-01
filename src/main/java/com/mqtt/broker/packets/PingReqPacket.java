package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.PINGREQ;

public final class PingReqPacket extends MqttPacket {

    public PingReqPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PINGREQ) {
            throw invalidPacketType(PingReqPacket.class);
        }
    }
}
