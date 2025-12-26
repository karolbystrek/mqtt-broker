package com.mqtt.broker.handler;

import com.mqtt.broker.packet.PubAckPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;

class PubAckPacketHandler implements PacketHandler<PubAckPacket> {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, PubAckPacket packet) throws IOException {
        return empty();
    }
}
