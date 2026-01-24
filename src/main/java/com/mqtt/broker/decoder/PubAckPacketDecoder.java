package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.PubAckPacket;

import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

class PubAckPacketDecoder implements PacketDecoder<PubAckPacket> {

    @Override
    public PubAckPacket decode(MqttFrame frame) {
        return new PubAckPacket(frame.fixedHeader(), decodeTwoByteInt(frame.body()));
    }
}
