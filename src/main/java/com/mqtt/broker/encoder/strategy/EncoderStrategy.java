package com.mqtt.broker.encoder.strategy;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface EncoderStrategy<T extends MqttPacket> {

    ByteBuffer encode(T packet);
}
