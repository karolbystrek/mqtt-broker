package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.PubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.EncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class PubAckEncoderStrategy implements EncoderStrategy<PubAckPacket> {

    @Override
    public ByteBuffer encode(PubAckPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) packet.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
