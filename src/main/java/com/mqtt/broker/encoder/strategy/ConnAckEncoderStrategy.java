package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.ConnAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.EncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class ConnAckEncoderStrategy implements EncoderStrategy<ConnAckPacket> {

    @Override
    public ByteBuffer encode(ConnAckPacket packet) {
        byte[] variableHeader = new byte[2];
        variableHeader[0] = (byte) (packet.getVariableHeader().isSessionPresent() ? 0x01 : 0x00);
        variableHeader[1] = (byte) packet.getVariableHeader().returnCode();

        var fixedHeaderBuffer = encodeFixedHeader(packet.getFixedHeader());

        var buffer = allocate(fixedHeaderBuffer.remaining() + variableHeader.length);
        buffer.put(fixedHeaderBuffer);
        buffer.put(variableHeader);
        buffer.flip();
        return buffer;
    }
}
