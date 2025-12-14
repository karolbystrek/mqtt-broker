package com.mqtt.broker.handler.strategy;

import com.mqtt.broker.handler.HandlerResult;
import com.mqtt.broker.packet.PubCompPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;

@Slf4j
public class PubCompPacketHandlerStrategy implements PacketHandlerStrategy<PubCompPacket> {
    @Override
    public HandlerResult handle(SocketChannel clientChannel, PubCompPacket packet) throws IOException {
        return empty();
    }
}
