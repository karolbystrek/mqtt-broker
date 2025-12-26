package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PingReqPacket;

import java.nio.ByteBuffer;

class PingReqPacketDecoder implements PacketDecoder<PingReqPacket> {

    @Override
    public PingReqPacket decode(MqttFixedHeader fixedHeader, ByteBuffer buffer) {
        return new PingReqPacket(fixedHeader);
    }
}
