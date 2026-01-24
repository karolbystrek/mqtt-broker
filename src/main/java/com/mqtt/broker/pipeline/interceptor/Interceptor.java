package com.mqtt.broker.pipeline.interceptor;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.pipeline.ProcessingResult;

import java.nio.channels.SocketChannel;

public interface Interceptor {

    void setNext(Interceptor next);

    ProcessingResult intercept(SocketChannel channel, MqttPacket packet);
}
