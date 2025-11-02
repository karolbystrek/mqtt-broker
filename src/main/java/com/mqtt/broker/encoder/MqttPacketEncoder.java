package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PingRespPacket;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubCompPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.SubAckPacket;
import com.mqtt.broker.packet.UnsubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static java.nio.ByteBuffer.allocate;

public class MqttPacketEncoder implements MqttPacketEncoderInterface, ConnAckPacketEncoder, SubAckPacketEncoder, PubAckPacketEncoder, PubRecPacketEncoder, PubCompPacketEncoder, PingRespPacketEncoder, UnsubAckPacketEncoder {

    @Override
    public ByteBuffer encode(MqttPacket mqttPacket) {
        return switch (mqttPacket) {
            case ConnAckPacket packet -> encodeConnAck(packet);
            case PingRespPacket packet -> encodePingResp(packet);
            case PubAckPacket packet -> encodePubAck(packet);
            case PubRecPacket packet -> encodePubRec(packet);
            case PubCompPacket packet -> encodePubComp(packet);
            case SubAckPacket packet -> encodeSubAck(packet);
            case UnsubAckPacket packet -> encodeUnsubAck(packet);
            default -> throw unsupportedPacketType(mqttPacket.getFixedHeader().packetType());
        };
    }

    @Override
    public ByteBuffer encodeFixedHeader(MqttFixedHeader header) {
        if (header.remainingLength() > 268435455) { // 256 MB
            throw new IllegalArgumentException("Remaining length exceeds maximum allowed size");
        }
        byte headerByte1 = (byte) ((header.packetType().getValue() << 4) | (header.flags() & 0x0F));

        var buffer = allocate(5); // Max 5 bytes for fixed header
        buffer.put(headerByte1);

        int value = header.remainingLength();
        do { // encode remaining length
            byte digit = (byte) (value % 128);
            value /= 128;
            if (value > 0) {
                digit |= (byte) 0x80;
            }
            buffer.put(digit);
        } while (value > 0);

        buffer.flip();
        return buffer;
    }

    @Override
    public void encodeString(ByteBuffer buffer, String s) {
        byte[] stringBytes = s.getBytes();
        buffer.putShort((short) stringBytes.length);
        buffer.put(stringBytes);
    }
}
