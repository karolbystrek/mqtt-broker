package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubRecPacket;

import java.nio.ByteBuffer;

public interface PubRecPacketDecoder extends MqttPacketDecoderInterface {

    default PubRecPacket decodePubRec(MqttFixedHeader header, ByteBuffer body) {
        return new PubRecPacket(header, decodeTwoByteInt(body));
    }
}
