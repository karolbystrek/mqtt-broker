package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PubCompPacket;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;

public interface PubCompPacketEncoder extends MqttPacketEncoderInterface {

    default ByteBuffer encodePubComp(PubCompPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) packet.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
