package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.PubRelPacket;

import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

class PubRelPacketDecoder implements PacketDecoder<PubRelPacket> {

    @Override
    public PubRelPacket decode(MqttFrame frame) {
        return new PubRelPacket(frame.fixedHeader(), decodeTwoByteInt(frame.body()));
    }
}
