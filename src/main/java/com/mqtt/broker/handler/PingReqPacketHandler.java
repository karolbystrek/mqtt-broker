package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PingReqPacket;
import com.mqtt.broker.packet.PingRespPacket;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.packet.MqttPacketType.PINGRESP;

class PingReqPacketHandler implements PacketHandler<PingReqPacket> {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, PingReqPacket packet) {
        return withResponse(new PingRespPacket(new MqttFixedHeader(PINGRESP, (byte) 0, 0)));
    }
}
