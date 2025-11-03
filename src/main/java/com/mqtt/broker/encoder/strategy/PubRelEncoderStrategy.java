package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubRelPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class PubRelEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        var pubRelPacket = (PubRelPacket) packet;

        var fixedHeaderBuffer = encodeFixedHeader(pubRelPacket.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) pubRelPacket.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
