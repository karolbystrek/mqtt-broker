package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PingReqPacket;
import com.mqtt.broker.packet.PingRespPacket;
import com.mqtt.broker.pipeline.ProcessingResult;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.packet.MqttPacketType.PINGRESP;
import static com.mqtt.broker.pipeline.ProcessingResult.withResponse;

class PingReqPacketHandler implements PacketHandler<PingReqPacket> {

    @Override
    public ProcessingResult handle(SocketChannel clientChannel, PingReqPacket packet) {
        return withResponse(new PingRespPacket(new MqttFixedHeader(PINGRESP, (byte) 0, 0)));
    }
}
