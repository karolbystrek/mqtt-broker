package com.mqtt.broker.interceptor;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.channels.SocketChannel;

public interface Pipeline {
    ProcessingResult process(SocketChannel channel, MqttPacket packet);
}
