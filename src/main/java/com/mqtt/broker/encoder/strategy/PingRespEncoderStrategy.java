package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.PingRespPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.EncoderUtils.encodeFixedHeader;

public final class PingRespEncoderStrategy implements EncoderStrategy<PingRespPacket> {

    @Override
    public ByteBuffer encode(PingRespPacket packet) {
        return encodeFixedHeader(packet.getFixedHeader());
    }
}
