package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

public final class MqttPacketEncoder {

    private final EncoderRegistry registry;

    public MqttPacketEncoder() {
        this.registry = new EncoderRegistry();
    }

    public ByteBuffer encode(MqttPacket packet) {
        return registry.getEncoder(packet.getFixedHeader().packetType()).encode(packet);
    }
}
