package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PubRelPacket;
import com.mqtt.broker.pipeline.ProcessingResult;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.packet.MqttPacketType.PUBREL;
import static com.mqtt.broker.pipeline.ProcessingResult.withResponse;

class PubRecPacketHandler implements PacketHandler<PubRecPacket> {

    @Override
    public ProcessingResult handle(SocketChannel clientChannel, PubRecPacket packet) throws IOException {
        var fixedHeader = new MqttFixedHeader(PUBREL, (byte) 2, 2);
        return withResponse(new PubRelPacket(fixedHeader, packet.packetIdentifier()));
    }
}
