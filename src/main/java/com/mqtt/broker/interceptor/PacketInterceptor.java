package com.mqtt.broker.interceptor;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.channels.SocketChannel;

public interface PacketInterceptor {

    void setNext(PacketInterceptor next);

    ProcessingResult intercept(SocketChannel channel, MqttPacket packet);
}
