package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PubAckPacket;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;

public interface PubAckPacketEncoder extends MqttPacketEncoderInterface {

    default ByteBuffer encodePubAck(PubAckPacket packet) {
        var buffer = allocate(4);
        var fixedHeader = encodeFixedHeader(packet.getFixedHeader());
        buffer.put(fixedHeader);

        buffer.putShort((short) packet.getPacketIdentifier());
        buffer.flip();
        return buffer;
    }
}
