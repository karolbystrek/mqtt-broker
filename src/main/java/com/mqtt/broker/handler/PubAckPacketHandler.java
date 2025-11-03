package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubAckPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;

public final class PubAckPacketHandler implements MqttPacketHandler {

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof PubAckPacket pubAckPacket)) {
            return empty();
        }

        System.out.println("Received PUBACK packet: " + pubAckPacket);

        // MQTT 3.1.1: PUBACK completes QoS 1 message delivery from broker to subscriber
        // No response needed - this is the final packet in QoS 1 flow
        // TODO: Remove packet from pending messages tracking

        return empty();
    }
}
