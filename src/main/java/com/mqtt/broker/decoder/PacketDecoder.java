package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttPacket;

interface PacketDecoder<T extends MqttPacket> {

    T decode(MqttFrame frame);
}
