package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

interface PacketDecoder<T extends MqttPacket> {

    T decode(MqttFixedHeader fixedHeader, ByteBuffer buffer);
}
