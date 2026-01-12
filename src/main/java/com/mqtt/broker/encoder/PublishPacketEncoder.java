package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PublishPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.EncoderUtils.encodeFixedHeader;
import static com.mqtt.broker.encoder.EncoderUtils.encodeString;
import static java.nio.ByteBuffer.allocate;

class PublishPacketEncoder implements PacketEncoder<PublishPacket> {

    @Override
    public ByteBuffer encode(PublishPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.fixedHeader());

        int variableHeaderLength = 2 + packet.variableHeader().topicName().length();
        if (packet.getQosLevel().requiresPacketId()) {
            variableHeaderLength += 2; // packet identifier
        }

        int remainingLength = variableHeaderLength + packet.payload().length;

        var buffer = allocate(fixedHeaderBuffer.remaining() + remainingLength);

        buffer.put(fixedHeaderBuffer);

        encodeString(buffer, packet.variableHeader().topicName());

        // Write packet identifier if QoS > 0
        if (packet.getQosLevel().requiresPacketId()) {
            buffer.putShort((short) packet.variableHeader().packetIdentifier());
        }

        buffer.put(packet.payload());
        buffer.flip();

        return buffer;
    }
}
