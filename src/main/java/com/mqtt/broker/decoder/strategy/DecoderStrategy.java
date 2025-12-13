package com.mqtt.broker.decoder.strategy;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

public interface DecoderStrategy<T extends MqttPacket> {

    T decode(MqttFixedHeader fixedHeader, ByteBuffer buffer);
}
