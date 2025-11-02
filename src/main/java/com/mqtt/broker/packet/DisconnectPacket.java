package com.mqtt.broker.packet;

import lombok.ToString;

import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.DISCONNECT;

@ToString
public final class DisconnectPacket extends MqttPacket {

    public DisconnectPacket(MqttFixedHeader fixedHeader) {
        super(fixedHeader);
        if (fixedHeader.packetType() != DISCONNECT) {
            throw invalidPacketType(DisconnectPacket.class);
        }
        
        // The Server MUST validate that reserved bits are set to zero
        if (fixedHeader.flags() != 0) {
            throw new IllegalArgumentException("DISCONNECT packet must have reserved bits set to zero (flags must be 0x00)");
        }
    }
}
