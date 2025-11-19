package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PubRelPacket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREL;

@Slf4j
public class PubRecPacketHandler implements MqttPacketHandler {
    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        var pubRecPacket = (PubRecPacket) packet;

        log.info("Received PUBREC packet: {}", pubRecPacket);

        // MQTT 3.1.1: When broker receives PUBREC from subscriber, respond with PUBREL
        // This continues the QoS 2 flow when broker is acting as publisher
        var fixedHeader = new MqttFixedHeader(PUBREL, (byte) 2, 2);
        return withResponse(new PubRelPacket(fixedHeader, pubRecPacket.getPacketIdentifier()));
    }
}
