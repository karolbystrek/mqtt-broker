package com.mqtt.broker.handler;

import com.mqtt.broker.packet.PubCompPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;

class PubCompPacketHandler implements PacketHandler<PubCompPacket> {
    @Override
    public HandlerResult handle(SocketChannel clientChannel, PubCompPacket packet) throws IOException {
        return empty();
    }
}
