package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Optional;

@FunctionalInterface
public interface MqttPacketHandler {

    Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) throws IOException;
}
