package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubAckPacket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;

@Slf4j
public final class PubAckPacketHandler implements MqttPacketHandler {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        var pubAckPacket = (PubAckPacket) packet;

        log.info("Received PUBACK packet: {}", pubAckPacket);

        return empty();
    }
}
