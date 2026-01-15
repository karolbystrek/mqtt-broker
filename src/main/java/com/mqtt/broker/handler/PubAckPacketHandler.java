package com.mqtt.broker.handler;

import com.mqtt.broker.interceptor.ProcessingResult;
import com.mqtt.broker.packet.PubAckPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.interceptor.ProcessingResult.empty;

class PubAckPacketHandler implements PacketHandler<PubAckPacket> {

    @Override
    public ProcessingResult handle(SocketChannel clientChannel, PubAckPacket packet) throws IOException {
        return empty();
    }
}
