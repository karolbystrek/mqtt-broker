package com.mqtt.broker.decoder;

import com.mqtt.broker.MqttFixedHeader;
import com.mqtt.broker.packet.PubAckPacket;

import java.nio.ByteBuffer;

public interface PubAckPacketDecoder extends MqttPacketDecoderInterface {

    default PubAckPacket decodePubAck(MqttFixedHeader fixedHeader, ByteBuffer body) {
        return new PubAckPacket(fixedHeader, decodeTwoByteInt(body));
    }
}
