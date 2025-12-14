package com.mqtt.broker.handler.strategy;

import com.mqtt.broker.handler.HandlerResult;
import com.mqtt.broker.packet.PubAckPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;

@Slf4j
public final class PubAckPacketHandlerStrategy implements PacketHandlerStrategy<PubAckPacket> {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, PubAckPacket packet) throws IOException {
        return empty();
    }
}
