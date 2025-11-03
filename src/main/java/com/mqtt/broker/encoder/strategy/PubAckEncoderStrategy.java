package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class PubAckEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        var pubAckPacket = (PubAckPacket) packet;

        var fixedHeaderBuffer = encodeFixedHeader(pubAckPacket.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);

        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) pubAckPacket.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
