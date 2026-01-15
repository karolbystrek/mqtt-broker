package com.mqtt.broker.handler;

import com.mqtt.broker.interceptor.ProcessingResult;
import com.mqtt.broker.packet.PubCompPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.interceptor.ProcessingResult.empty;

class PubCompPacketHandler implements PacketHandler<PubCompPacket> {
    @Override
    public ProcessingResult handle(SocketChannel clientChannel, PubCompPacket packet) throws IOException {
        return empty();
    }
}
