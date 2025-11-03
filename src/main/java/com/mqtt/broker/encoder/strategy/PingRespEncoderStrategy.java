package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.PacketEncoderUtils.encodeFixedHeader;

public final class PingRespEncoderStrategy implements PacketEncoderStrategy {

    @Override
    public ByteBuffer encode(MqttPacket packet) {
        return encodeFixedHeader(packet.getFixedHeader());
    }
}
