package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.UnsubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class UnsubAckEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        var unsubAckPacket = (UnsubAckPacket) packet;

        var fixedHeaderBuffer = encodeFixedHeader(unsubAckPacket.getFixedHeader());

        var fullPacket = allocate(fixedHeaderBuffer.remaining() + 2);
        fullPacket.put(fixedHeaderBuffer);
        fullPacket.putShort((short) unsubAckPacket.getPacketIdentifier());
        fullPacket.flip();

        return fullPacket;
    }
}
