package com.mqtt.broker.packet;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.DISCONNECT;

public record DisconnectPacket(MqttFixedHeader fixedHeader) implements MqttPacket {
    public DisconnectPacket {
        if (fixedHeader.packetType() != DISCONNECT) {
            throw invalidPacketType(DisconnectPacket.class);
        }

        // The Server MUST validate that reserved bits are set to zero
        if (fixedHeader.flags() != 0) {
            throw new IllegalArgumentException("DISCONNECT packet must have reserved bits set to zero (flags must be 0x00)");
        }
    }
}
