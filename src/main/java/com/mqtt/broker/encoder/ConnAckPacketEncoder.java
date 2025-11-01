package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.ConnAckPacket;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;

public interface ConnAckPacketEncoder extends MqttPacketEncoderInterface {

    default ByteBuffer encodeConnAck(ConnAckPacket packet) {
        byte[] variableHeader = new byte[2];
        variableHeader[0] = (byte) (packet.getVariableHeader().isSessionPresent() ? 0x01 : 0x00);
        variableHeader[1] = (byte) packet.getVariableHeader().returnCode();

        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());
        
        return allocate(fixedHeaderBuffer.remaining() + variableHeader.length)
                .put(fixedHeaderBuffer)
                .put(variableHeader);
    }
}
