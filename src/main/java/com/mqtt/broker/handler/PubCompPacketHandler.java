package com.mqtt.broker.handler;

import com.mqtt.broker.packet.PubCompPacket;
import com.mqtt.broker.pipeline.ProcessingResult;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.pipeline.ProcessingResult.empty;

class PubCompPacketHandler implements PacketHandler<PubCompPacket> {
    @Override
    public ProcessingResult handle(SocketChannel clientChannel, PubCompPacket packet) throws IOException {
        return empty();
    }
}
