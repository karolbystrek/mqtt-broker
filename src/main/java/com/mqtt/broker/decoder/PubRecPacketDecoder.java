package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.PubRecPacket;

import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

class PubRecPacketDecoder implements PacketDecoder<PubRecPacket> {

    @Override
    public PubRecPacket decode(MqttFrame frame) {
        return new PubRecPacket(frame.fixedHeader(), decodeTwoByteInt(frame.body()));
    }
}
