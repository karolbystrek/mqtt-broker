package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubCompPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class PubCompEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        var pubCompPacket = (PubCompPacket) packet;

        var fixedHeaderBuffer = encodeFixedHeader(pubCompPacket.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) pubCompPacket.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
