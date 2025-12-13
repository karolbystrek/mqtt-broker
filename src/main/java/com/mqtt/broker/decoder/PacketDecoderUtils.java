package com.mqtt.broker.decoder;

import java.nio.ByteBuffer;

import com.mqtt.broker.packet.MqttControlPacketType;
import com.mqtt.broker.packet.MqttFixedHeader;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PacketDecoderUtils {

    private PacketDecoderUtils() {}

    public static MqttFixedHeader decodeFixedHeader(ByteBuffer buffer) {
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
                throw new IllegalArgumentException("Malformed remaining length");
            }
        } while ((digit & 0x80) != 0);

        if (buffer.remaining() < remainingLength) {
            return null;
        }

        return new MqttFixedHeader(packetType, flags, remainingLength);
    }

    public static String decodeString(ByteBuffer buffer) {
        int length = decodeTwoByteInt(buffer);
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, UTF_8);
    }

    public static int decodeTwoByteInt(ByteBuffer buffer) {
        return buffer.getShort() & 0xFFFF;
    }
}
