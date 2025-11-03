package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PingReqPacket;
import com.mqtt.broker.packet.PingRespPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGRESP;

public final class PingReqPacketHandler implements MqttPacketHandler {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof PingReqPacket pingReqPacket)) {
            return empty();
        }

        System.out.println("Received PINGREQ packet: " + pingReqPacket);

        return withResponse(new PingRespPacket(new MqttFixedHeader(PINGRESP, (byte) 0, 0)));
    }
}
