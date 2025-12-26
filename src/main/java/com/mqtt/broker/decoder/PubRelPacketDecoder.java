package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubRelPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

class PubRelPacketDecoder implements PacketDecoder<PubRelPacket> {

    @Override
    public PubRelPacket decode(MqttFixedHeader header, ByteBuffer body) {
        return new PubRelPacket(header, decodeTwoByteInt(body));
    }
}
