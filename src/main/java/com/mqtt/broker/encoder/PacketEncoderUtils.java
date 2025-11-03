package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.MqttFixedHeader;

import java.nio.ByteBuffer;

import static java.nio.ByteBuffer.allocate;

public final class PacketEncoderUtils {

    private PacketEncoderUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ByteBuffer encodeFixedHeader(MqttFixedHeader header) {
        if (header.remainingLength() > 268435455) { // 256 MB
            throw new IllegalArgumentException("Remaining length exceeds maximum allowed size");
        }

        byte headerByte1 = (byte) ((header.packetType().getValue() << 4) | (header.flags() & 0x0F));

        int remainingLength = header.remainingLength();
        int remainingLengthBytes = calculateRemainingLengthBytes(remainingLength);

        var buffer = allocate(1 + remainingLengthBytes);
        buffer.put(headerByte1);

        encodeRemainingLength(buffer, remainingLength);

        buffer.flip();
        return buffer;
    }

    public static void encodeString(ByteBuffer buffer, String s) {
        byte[] stringBytes = s.getBytes();
        buffer.putShort((short) stringBytes.length);
        buffer.put(stringBytes);
    }

    private static int calculateRemainingLengthBytes(int remainingLength) {
        if (remainingLength == 0) {
            return 1;
        }

        int bytes = 0;
        int temp = remainingLength;
        while (temp > 0) {
            temp /= 128;
            bytes++;
        }
        return bytes;
    }

    private static void encodeRemainingLength(ByteBuffer buffer, int value) {
        do {
            byte digit = (byte) (value % 128);
            value /= 128;
            if (value > 0) {
                digit |= (byte) 0x80;
            }
            buffer.put(digit);
        } while (value > 0);
    }
}
