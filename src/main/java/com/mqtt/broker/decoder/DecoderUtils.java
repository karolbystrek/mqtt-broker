package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacketType;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

class DecoderUtils {

    private DecoderUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static MqttFixedHeader decodeFixedHeader(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return null;
        }
        byte headerByte1 = buffer.get();
        var packetType = MqttPacketType.fromHeaderByte(headerByte1);
        byte flags = (byte) (headerByte1 & 0x0F);

        int remainingLength = 0;
        int multiplier = 1;
        byte digit;
        int bytesConsumed = 0;
        do {
            if (!buffer.hasRemaining()) {
                return null; // Incomplete length bytes
            }
            digit = buffer.get();
            bytesConsumed++;
            remainingLength += (digit & 0x7F) * multiplier;
            multiplier *= 128;
            if (bytesConsumed > 4) {
                throw new IllegalArgumentException("Malformed remaining length");
            }
        } while ((digit & 0x80) != 0);

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
