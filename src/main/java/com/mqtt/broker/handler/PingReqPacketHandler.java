package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PingReqPacket;
import com.mqtt.broker.packet.PingRespPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Optional;

import static com.mqtt.broker.packet.MqttControlPacketType.PINGRESP;
import static java.util.Optional.of;

public final class PingReqPacketHandler implements MqttPacketHandler {

    @Override
    public Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof PingReqPacket pingReqPacket)) {
            return Optional.empty();
        }

        System.out.println("Received PINGREQ packet: " + pingReqPacket);

        return of(new PingRespPacket(new MqttFixedHeader(PINGRESP, (byte) 0, 0)));
    }
}
