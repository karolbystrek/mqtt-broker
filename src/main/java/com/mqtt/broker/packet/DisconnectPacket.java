package com.mqtt.broker.packet;

import com.mqtt.broker.MqttFixedHeader;

import static com.mqtt.broker.MqttControlPacketType.DISCONNECT;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

public final class DisconnectPacket extends MqttPacket {

    public DisconnectPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != DISCONNECT) {
            throw invalidPacketType(DisconnectPacket.class);
        }
    }
}
