package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@FunctionalInterface
interface PacketHandler<T extends MqttPacket> {

    HandlerResult handle(SocketChannel channel, T packet) throws IOException;
}
