package com.mqtt.broker.packet;

import static com.mqtt.broker.packet.MqttControlPacketType.DISCONNECT;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;

public final class DisconnectPacket extends MqttPacket {

    public DisconnectPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != DISCONNECT) {
            throw invalidPacketType(DisconnectPacket.class);
        }
    }
}
