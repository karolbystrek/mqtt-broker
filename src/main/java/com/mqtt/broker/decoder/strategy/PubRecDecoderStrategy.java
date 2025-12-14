package com.mqtt.broker.decoder.strategy;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubRecPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

public class PubRecDecoderStrategy implements DecoderStrategy<PubRecPacket> {

    @Override
    public PubRecPacket decode(MqttFixedHeader header, ByteBuffer body) {
        return new PubRecPacket(header, decodeTwoByteInt(body));
    }
}
