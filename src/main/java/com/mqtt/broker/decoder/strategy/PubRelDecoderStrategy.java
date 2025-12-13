package com.mqtt.broker.decoder.strategy;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubRelPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.decoder.PacketDecoderUtils.decodeTwoByteInt;

public class PubRelDecoderStrategy implements DecoderStrategy<PubRelPacket> {

    @Override
    public PubRelPacket decode(MqttFixedHeader header, ByteBuffer body) {
        return new PubRelPacket(header, decodeTwoByteInt(body));
    }
}
