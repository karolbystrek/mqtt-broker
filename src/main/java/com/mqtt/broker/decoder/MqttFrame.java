package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;

import java.nio.ByteBuffer;
import java.util.Optional;

import static com.mqtt.broker.decoder.DecoderUtils.decodeFixedHeader;

public record MqttFrame(MqttFixedHeader fixedHeader, ByteBuffer body) {

    public static Optional<MqttFrame> read(ByteBuffer buffer) {
        if (buffer.remaining() < 2) {
            return Optional.empty();
        }

        buffer.mark();
        MqttFixedHeader fixedHeader = decodeFixedHeader(buffer);

        if (fixedHeader == null) {
            buffer.reset();
            return Optional.empty();
        }

        if (buffer.remaining() < fixedHeader.remainingLength()) {
            buffer.reset();
            return Optional.empty();
        }

        ByteBuffer body = buffer.slice();
        body.limit(fixedHeader.remainingLength());
        buffer.position(buffer.position() + fixedHeader.remainingLength());

        return Optional.of(new MqttFrame(fixedHeader, body));
    }
}
