package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.SubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

public final class SubAckEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        var subAckPacket = (SubAckPacket) packet;

        int payloadSize = subAckPacket.getGrantedQosLevels().size();
        var payload = allocate(payloadSize);
        subAckPacket.getGrantedQosLevels().forEach(qos -> payload.put(qos.byteValue()));
        payload.flip();

        int variableHeaderSize = 2; // Packet Identifier
        int remainingLength = variableHeaderSize + payloadSize;

        var fixedHeader = encodeFixedHeader(subAckPacket.getFixedHeader());

        var fullPacket = allocate(fixedHeader.remaining() + remainingLength);
        fullPacket.put(fixedHeader);
        fullPacket.putShort((short) subAckPacket.getPacketIdentifier());
        fullPacket.put(payload);
        fullPacket.flip();

        return fullPacket;
    }
}
