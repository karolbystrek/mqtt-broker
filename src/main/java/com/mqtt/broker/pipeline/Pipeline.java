package com.mqtt.broker.pipeline;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.channels.SocketChannel;

public interface Pipeline {
    ProcessingResult process(SocketChannel channel, MqttPacket packet);
}
