package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

class PubAckPacketDecoder implements PacketDecoder<PubAckPacket> {

    @Override
    public PubAckPacket decode(MqttFixedHeader fixedHeader, ByteBuffer body) {
        return new PubAckPacket(fixedHeader, decodeTwoByteInt(body));
    }
}
