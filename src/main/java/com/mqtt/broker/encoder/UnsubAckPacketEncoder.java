package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.UnsubAckPacket;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;

public interface UnsubAckPacketEncoder extends MqttPacketEncoderInterface {

    default ByteBuffer encodeUnsubAck(UnsubAckPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);
        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) packet.getPacketIdentifier());

        return fullPacket;
    }
}
