package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubRelPacket;

import java.nio.ByteBuffer;

public interface PubRelPacketDecoder extends MqttPacketDecoderInterface {

    default PubRelPacket decodePubRel(MqttFixedHeader header, ByteBuffer body) {
        return new PubRelPacket(header, decodeTwoByteInt(body));
    }
}
