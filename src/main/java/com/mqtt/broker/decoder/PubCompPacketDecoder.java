package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubCompPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

class PubCompPacketDecoder implements PacketDecoder<PubCompPacket> {

    @Override
    public PubCompPacket decode(MqttFixedHeader fixedHeader, ByteBuffer body) {
        return new PubCompPacket(fixedHeader, decodeTwoByteInt(body));
    }
}
