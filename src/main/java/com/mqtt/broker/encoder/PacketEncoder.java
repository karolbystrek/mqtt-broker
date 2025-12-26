package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

@FunctionalInterface
interface PacketEncoder<T extends MqttPacket> {

    ByteBuffer encode(T packet);
}
