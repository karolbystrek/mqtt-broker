package com.mqtt.broker.decoder;

import com.mqtt.broker.MqttFixedHeader;
import com.mqtt.broker.packet.PubRecPacket;

import java.nio.ByteBuffer;

public interface PubRecPacketDecoder extends MqttPacketDecoderInterface {

    default PubRecPacket decodePubRec(MqttFixedHeader fixedHeader, ByteBuffer body) {
        return new PubRecPacket(fixedHeader, decodeTwoByteInt(body));
    }
}
