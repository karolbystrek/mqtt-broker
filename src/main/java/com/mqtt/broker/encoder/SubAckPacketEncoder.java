package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.SubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.EncoderUtils.encodeFixedHeader;
import static java.nio.ByteBuffer.allocate;

class SubAckPacketEncoder implements PacketEncoder<SubAckPacket> {

    @Override
    public ByteBuffer encode(SubAckPacket packet) {
        int payloadSize = packet.grantedQosLevels().size();
        var payload = allocate(payloadSize);
        packet.grantedQosLevels().forEach(qos -> payload.put(qos.byteValue()));
        payload.flip();

        int variableHeaderSize = 2; // Packet Identifier
        int remainingLength = variableHeaderSize + payloadSize;

        var fixedHeader = encodeFixedHeader(packet.fixedHeader());

        var fullPacket = allocate(fixedHeader.remaining() + remainingLength);
        fullPacket.put(fixedHeader);
        fullPacket.putShort((short) packet.packetIdentifier());
        fullPacket.put(payload);
        fullPacket.flip();

        return fullPacket;
    }
}
