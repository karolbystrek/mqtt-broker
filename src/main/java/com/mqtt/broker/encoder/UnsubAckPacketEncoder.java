package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.UnsubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.EncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

class UnsubAckPacketEncoder implements PacketEncoder<UnsubAckPacket> {

    @Override
    public ByteBuffer encode(UnsubAckPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);
        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) packet.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
