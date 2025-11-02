package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PublishPacket;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;

public interface PublishPacketEncoder extends MqttPacketEncoderInterface {

    default ByteBuffer encodePublish(PublishPacket packet) {
        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        int variableHeaderLength = 2 + packet.getVariableHeader().topicName().length();
        if (packet.getQosLevel().requiresPacketId()) {
            variableHeaderLength += 2; // packet identifier
        }

        int remainingLength = variableHeaderLength + packet.getPayload().length;


        var buffer = allocate(fixedHeaderBuffer.remaining() + remainingLength);

        buffer.put(fixedHeaderBuffer);

        encodeString(buffer, packet.getVariableHeader().topicName());

        // Write packet identifier if QoS > 0
        if (packet.getQosLevel().requiresPacketId()) {
            buffer.putShort((short) packet.getVariableHeader().packetIdentifier());
        }

        buffer.put(packet.getPayload());

        return buffer;
    }
}
