package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@FunctionalInterface
public interface MqttPacketHandler {

    void handle(SocketChannel clientChannel, MqttPacket packet) throws IOException;

}
