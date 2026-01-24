package com.mqtt.broker.pipeline.interceptor;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.handler.MqttPacketHandler;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.pipeline.ProcessingResult;

import java.nio.channels.SocketChannel;
import java.util.Optional;

public class PacketHandlingInterceptor extends ChainablePacketInterceptor {

    private final MqttPacketHandler packetHandler;

    public PacketHandlingInterceptor(BrokerContext context) {
        this.packetHandler = new MqttPacketHandler(context);
    }

    @Override
    protected Optional<ProcessingResult> process(SocketChannel channel, MqttPacket packet) {
        return Optional.of(packetHandler.handle(channel, packet));
    }
}
