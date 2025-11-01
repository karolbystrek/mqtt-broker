package com.mqtt.broker.decoder;

import com.mqtt.broker.MqttControlPacketType;
import com.mqtt.broker.MqttFixedHeader;
import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PingReqPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.exception.MalformedRemainingLengthException.malformedRemainingLength;
import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static java.nio.charset.StandardCharsets.UTF_8;

public class MqttPacketDecoder implements ConnectPacketDecoder, PublishPacketDecoder, SubscribePacketDecoder, UnsubscribePacketDecoder, PubAckPacketDecoder, PubRecPacketDecoder, PubRelPacketDecoder, PubCompPacketDecoder {

    public MqttPacket decode(ByteBuffer buffer) {
        if (buffer.remaining() < 2) {
            return null; // Not enough data to read fixed header + one byte of remaining length
        }

        buffer.mark(); // mark the current position in case of incomplete packet

        byte headerByte1 = buffer.get();
        var packetType = MqttControlPacketType.fromHeaderByte(headerByte1);
        byte flags = (byte) (headerByte1 & 0x0F);

        int remainingLength = 0;
        int multiplier = 1;
        byte digit;
        int bytesConsumed = 0;
        do {
            if (!buffer.hasRemaining()) {
                buffer.reset(); // reset to marked position and wait for more data
                return null;
            }
            digit = buffer.get();
            bytesConsumed++;
            remainingLength += (digit & 0x7F) * multiplier;
            multiplier *= 128;
            if (bytesConsumed > 4) {
                throw malformedRemainingLength();
            }
        } while ((digit & 0x80) != 0);

        if (buffer.remaining() < remainingLength) {
            buffer.reset(); // we have the full fixed header but not the full packet
            return null;
        }

        var fixedHeader = new MqttFixedHeader(packetType, flags, remainingLength);

        ByteBuffer packetBody = buffer.slice();
        packetBody.limit(remainingLength);

        buffer.position(buffer.position() + remainingLength);

        return switch (packetType) {
            case CONNECT -> decodeConnect(fixedHeader, packetBody);
            case PUBLISH -> decodePublish(fixedHeader, packetBody);
            case SUBSCRIBE -> decodeSubscribe(fixedHeader, packetBody);
            case UNSUBSCRIBE -> decodeUnsubscribe(fixedHeader, packetBody);
            case PINGREQ -> new PingReqPacket(fixedHeader);
            case DISCONNECT -> new DisconnectPacket(fixedHeader);
            case PUBACK -> decodePubAck(fixedHeader, packetBody);
            case PUBREC -> decodePubRec(fixedHeader, packetBody);
            case PUBREL -> decodePubRel(fixedHeader, packetBody);
            case PUBCOMP -> decodePubComp(fixedHeader, packetBody);
            default -> throw unsupportedPacketType(packetType);
        };
    }

    @Override
    public String decodeString(ByteBuffer buffer) {
        int length = decodeTwoByteInt(buffer);
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, UTF_8);
    }

    @Override
    public int decodeTwoByteInt(ByteBuffer buffer) {
        return buffer.getShort() & 0xFFFF;
    }
}
