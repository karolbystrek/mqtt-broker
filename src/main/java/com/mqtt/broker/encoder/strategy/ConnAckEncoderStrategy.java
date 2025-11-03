package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class ConnAckEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        var connAckPacket = (ConnAckPacket) packet;

        byte[] variableHeader = new byte[2];
        variableHeader[0] = (byte) (connAckPacket.getVariableHeader().isSessionPresent() ? 0x01 : 0x00);
        variableHeader[1] = (byte) connAckPacket.getVariableHeader().returnCode();

        var fixedHeaderBuffer = encodeFixedHeader(connAckPacket.getFixedHeader());

        var buffer = allocate(fixedHeaderBuffer.remaining() + variableHeader.length);
        buffer.put(fixedHeaderBuffer);
        buffer.put(variableHeader);
        buffer.flip();
        return buffer;
    }
}
