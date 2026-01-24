package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacketType;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

class DecoderUtils {

    private static final int FLAGS_MASK = 0x0F;
    private static final int LENGTH_VALUE_MASK = 0x7F;
    private static final int LENGTH_CONTINUATION_MASK = 0x80;
    private static final int LENGTH_MULTIPLIER_BASE = 128;
    private static final int MAX_LENGTH_BYTES = 4;
    private static final int UNSIGNED_SHORT_MASK = 0xFFFF;

    private DecoderUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static MqttFixedHeader decodeFixedHeader(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return null;
        }
        byte headerByte1 = buffer.get();
        var packetType = MqttPacketType.fromHeaderByte(headerByte1);
        byte flags = (byte) (headerByte1 & FLAGS_MASK);

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
            remainingLength += (digit & LENGTH_VALUE_MASK) * multiplier;
            multiplier *= LENGTH_MULTIPLIER_BASE;
            if (bytesConsumed > MAX_LENGTH_BYTES) {
                throw new IllegalArgumentException("Malformed remaining length");
            }
        } while ((digit & LENGTH_CONTINUATION_MASK) != 0);

        return new MqttFixedHeader(packetType, flags, remainingLength);
    }

    public static String decodeString(ByteBuffer buffer) {
        int length = decodeTwoByteInt(buffer);
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, UTF_8);
    }

    public static int decodeTwoByteInt(ByteBuffer buffer) {
        return buffer.getShort() & UNSIGNED_SHORT_MASK;
    }
}
