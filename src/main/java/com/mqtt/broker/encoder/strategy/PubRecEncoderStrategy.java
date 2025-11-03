package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubRecPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class PubRecEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        var pubRecPacket = (PubRecPacket) packet;

        var fixedHeaderBuffer = encodeFixedHeader(pubRecPacket.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) pubRecPacket.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
