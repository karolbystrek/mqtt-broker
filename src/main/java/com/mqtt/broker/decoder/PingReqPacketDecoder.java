package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.PingReqPacket;

class PingReqPacketDecoder implements PacketDecoder<PingReqPacket> {

    @Override
    public PingReqPacket decode(MqttFrame frame) {
        return new PingReqPacket(frame.fixedHeader());
    }
}
