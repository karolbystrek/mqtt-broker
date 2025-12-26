package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttFixedHeader;

import java.nio.ByteBuffer;

class DisconnectPacketDecoder implements PacketDecoder<DisconnectPacket> {

    @Override
    public DisconnectPacket decode(MqttFixedHeader fixedHeader, ByteBuffer buffer) {
        return new DisconnectPacket(fixedHeader);
    }
}
