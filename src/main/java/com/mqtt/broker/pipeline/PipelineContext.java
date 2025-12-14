package com.mqtt.broker.pipeline;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.handler.HandlerResult;
import com.mqtt.broker.packet.MqttPacket;
import lombok.Getter;
import lombok.Setter;

import java.nio.channels.SocketChannel;

@Getter
@Setter
public class PipelineContext {
    private final SocketChannel clientChannel;
    private final MqttPacket packet;
    private final BrokerContext brokerContext;
    private HandlerResult handlerResult;
    private boolean terminated = false;

    public PipelineContext(SocketChannel clientChannel, MqttPacket packet, BrokerContext brokerContext) {
        this.clientChannel = clientChannel;
        this.packet = packet;
        this.brokerContext = brokerContext;
    }

    public void terminate() {
        this.terminated = true;
    }
}
