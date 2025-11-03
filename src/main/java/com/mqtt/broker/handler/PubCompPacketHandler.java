package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubCompPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;

public class PubCompPacketHandler implements MqttPacketHandler {
    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof PubCompPacket pubCompPacket)) {
            return empty();
        }

        System.out.println("Handling PUBCOMP packet: " + pubCompPacket);
        // MQTT 3.1.1: PUBCOMP completes QoS 2 message delivery from broker to subscriber
        // No response needed - this is the final packet in QoS 2 flow
        // TODO: Remove packet from pending messages tracking

        return empty();
    }
}
