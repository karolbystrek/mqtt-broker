package com.mqtt.broker.handler.strategy;

import com.mqtt.broker.handler.HandlerResult;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PingReqPacket;
import com.mqtt.broker.packet.PingRespPacket;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGRESP;


@Slf4j
public final class PingReqPacketHandlerStrategy implements PacketHandlerStrategy<PingReqPacket> {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, PingReqPacket packet) {
        return withResponse(new PingRespPacket(new MqttFixedHeader(PINGRESP, (byte) 0, 0)));
    }
}
