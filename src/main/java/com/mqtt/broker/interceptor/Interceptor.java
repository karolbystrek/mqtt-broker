package com.mqtt.broker.interceptor;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.channels.SocketChannel;

public interface Interceptor {

    void setNext(Interceptor next);

    ProcessingResult intercept(SocketChannel channel, MqttPacket packet);
}
