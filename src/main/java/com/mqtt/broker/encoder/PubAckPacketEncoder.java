package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PubAckPacket;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;

public interface PubAckPacketEncoder extends MqttPacketEncoderInterface {

    default ByteBuffer encodePubAck(PubAckPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) packet.getPacketIdentifier());

        return fullPacket;
    }
}
