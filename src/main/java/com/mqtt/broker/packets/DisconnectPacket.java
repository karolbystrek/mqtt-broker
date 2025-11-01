package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.DISCONNECT;

public final class DisconnectPacket extends MqttPacket {

    public DisconnectPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != DISCONNECT) {
            throw invalidPacketType(DisconnectPacket.class);
        }
    }
}
