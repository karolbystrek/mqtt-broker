package com.mqtt.broker.handler.strategy;

import com.mqtt.broker.handler.HandlerResult;
import com.mqtt.broker.packet.MqttPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@FunctionalInterface
public interface PacketHandlerStrategy<T extends MqttPacket> {

    HandlerResult handle(SocketChannel channel, T packet) throws IOException;
}
