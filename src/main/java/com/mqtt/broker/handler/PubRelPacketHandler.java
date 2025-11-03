package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubCompPacket;
import com.mqtt.broker.packet.PubRelPacket;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBCOMP;

public class PubRelPacketHandler implements MqttPacketHandler {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) {
        if (!(packet instanceof PubRelPacket pubRelPacket)) {
            return empty();
        }

        System.out.println("Handling PUBREL packet: " + pubRelPacket);

        // MQTT 3.1.1: When broker receives PUBREL from publisher, respond with PUBCOMP
        // This completes the QoS 2 flow when broker is acting as subscriber/receiver
        // The message should have been delivered to subscribers when PUBLISH was received
        var fixedHeader = new MqttFixedHeader(PUBCOMP, (byte) 0, 2);
        return withResponse(new PubCompPacket(fixedHeader, pubRelPacket.getPacketIdentifier()));
    }
}
