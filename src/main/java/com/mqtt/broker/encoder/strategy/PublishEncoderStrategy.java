package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PublishPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;
import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeString;
import static java.nio.ByteBuffer.allocate;

public final class PublishEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        var publishPacket = (PublishPacket) packet;

        var fixedHeaderBuffer = encodeFixedHeader(publishPacket.getFixedHeader());

        int variableHeaderLength = 2 + publishPacket.getVariableHeader().topicName().length();
        if (publishPacket.getQosLevel().requiresPacketId()) {
            variableHeaderLength += 2; // packet identifier
        }

        int remainingLength = variableHeaderLength + publishPacket.getPayload().length;

        var buffer = allocate(fixedHeaderBuffer.remaining() + remainingLength);

        buffer.put(fixedHeaderBuffer);

        encodeString(buffer, publishPacket.getVariableHeader().topicName());

        // Write packet identifier if QoS > 0
        if (publishPacket.getQosLevel().requiresPacketId()) {
            buffer.putShort((short) publishPacket.getVariableHeader().packetIdentifier());
        }

        buffer.put(publishPacket.getPayload());
        buffer.flip();

        return buffer;
    }
}
