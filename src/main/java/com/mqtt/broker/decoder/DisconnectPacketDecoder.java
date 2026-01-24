package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.DisconnectPacket;

class DisconnectPacketDecoder implements PacketDecoder<DisconnectPacket> {

    @Override
    public DisconnectPacket decode(MqttFrame frame) {
        return new DisconnectPacket(frame.fixedHeader());
    }
}
