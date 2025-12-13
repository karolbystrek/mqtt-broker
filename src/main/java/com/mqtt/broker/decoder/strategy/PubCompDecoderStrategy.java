package com.mqtt.broker.decoder.strategy;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubCompPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.decoder.PacketDecoderUtils.decodeTwoByteInt;

public class PubCompDecoderStrategy implements DecoderStrategy<PubCompPacket> {

    @Override
    public PubCompPacket decode(MqttFixedHeader fixedHeader, ByteBuffer body) {
        return new PubCompPacket(fixedHeader, decodeTwoByteInt(body));
    }
}
