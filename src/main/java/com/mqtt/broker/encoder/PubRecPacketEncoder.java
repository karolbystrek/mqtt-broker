package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PubRecPacket;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;

public interface PubRecPacketEncoder extends MqttPacketEncoderInterface {

    default ByteBuffer encodePubRec(PubRecPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) packet.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
