package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubCompPacket;
import com.mqtt.broker.packet.PubRelPacket;

import java.nio.channels.SocketChannel;
import java.util.Optional;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBCOMP;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class PubRelPacketHandler implements MqttPacketHandler {

    @Override
    public Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) {
        if (!(packet instanceof PubRelPacket pubRelPacket)) {
            return empty();
        }

        System.out.println("Handling PUBREL packet: " + pubRelPacket);

        // TODO: Always respond with PUBCOMP to complete QoS 2 flow?
        var fixedHeader = new MqttFixedHeader(PUBCOMP, (byte) 0, 2);
        return of(new PubCompPacket(fixedHeader, pubRelPacket.getPacketIdentifier()));
    }
}
