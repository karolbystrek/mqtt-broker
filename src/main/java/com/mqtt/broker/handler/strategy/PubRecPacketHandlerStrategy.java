package com.mqtt.broker.handler.strategy;

import com.mqtt.broker.handler.HandlerResult;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PubRelPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.packet.MqttPacketType.PUBREL;

@Slf4j
public class PubRecPacketHandlerStrategy implements PacketHandlerStrategy<PubRecPacket> {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, PubRecPacket packet) throws IOException {
        var fixedHeader = new MqttFixedHeader(PUBREL, (byte) 2, 2);
        return withResponse(new PubRelPacket(fixedHeader, packet.getPacketIdentifier()));
    }
}
