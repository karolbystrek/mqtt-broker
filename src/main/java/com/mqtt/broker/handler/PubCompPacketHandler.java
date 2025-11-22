package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubCompPacket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;

@Slf4j
public class PubCompPacketHandler implements MqttPacketHandler {
    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        var pubCompPacket = (PubCompPacket) packet;

        log.info("Handling PUBCOMP packet: {}", pubCompPacket);

        return empty();
    }
}
