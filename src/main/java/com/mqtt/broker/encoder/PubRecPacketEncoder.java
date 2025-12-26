package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PubRecPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.EncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

class PubRecPacketEncoder implements PacketEncoder<PubRecPacket> {

    @Override
    public ByteBuffer encode(PubRecPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) packet.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
