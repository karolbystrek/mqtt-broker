package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.PubCompPacket;

import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

class PubCompPacketDecoder implements PacketDecoder<PubCompPacket> {

    @Override
    public PubCompPacket decode(MqttFrame frame) {
        return new PubCompPacket(frame.fixedHeader(), decodeTwoByteInt(frame.body()));
    }
}
