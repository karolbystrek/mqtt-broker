package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.SubAckPacket;
import com.mqtt.broker.packet.SubscribePacket;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mqtt.broker.packet.MqttControlPacketType.SUBACK;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@RequiredArgsConstructor
public class SubscribePacketHandler implements MqttPacketHandler {

    private final Map<SocketChannel, Session> activeSessions;

    @Override
    public Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof SubscribePacket subscribePacket)) {
            return empty();
        }

        System.out.println("Received SUBSCRIBE packet: " + subscribePacket);

        Session session = activeSessions.get(clientChannel);
        if (session == null) {
            System.err.println("No session found for channel: " + clientChannel.getRemoteAddress());
            throw new IllegalStateException("Session not found for channel");
        }

        List<Integer> grantedQosLevels = subscribePacket.getSubscriptions().stream()
                .map(subscription -> {
                    session.addSubscription(subscription.topicFilter(), subscription.qos());

                    System.out.println("Client " + session.getClientId() + " subscribed to topic: "
                            + subscription.topicFilter() + " with QoS: " + subscription.qos());
                    
                    return subscription.qos().getValue();
                })
                .toList();

        var fixedHeader = new MqttFixedHeader(SUBACK, (byte) 0, 2 + grantedQosLevels.size());
        return of(new SubAckPacket(fixedHeader, subscribePacket.getPacketIdentifier(), grantedQosLevels));
    }
}
