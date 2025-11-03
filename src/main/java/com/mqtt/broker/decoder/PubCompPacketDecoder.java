package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubCompPacket;

import java.nio.ByteBuffer;

public interface PubCompPacketDecoder extends MqttPacketDecoderInterface {

    default PubCompPacket decodePubComp(MqttFixedHeader fixedHeader, ByteBuffer body) {
        return new PubCompPacket(fixedHeader, decodeTwoByteInt(body));
    }
}
