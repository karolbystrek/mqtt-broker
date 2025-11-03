package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.SubAckPacket;
import com.mqtt.broker.packet.SubscribePacket;
import com.mqtt.broker.trie.TopicTree;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.packet.MqttControlPacketType.SUBACK;

@RequiredArgsConstructor
public class SubscribePacketHandler implements MqttPacketHandler {

    private final Map<SocketChannel, Session> activeSessions;
    private final TopicTree topicTree;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof SubscribePacket subscribePacket)) {
            return empty();
        }

        System.out.println("Received SUBSCRIBE packet: " + subscribePacket);

        Session session = activeSessions.get(clientChannel);
        if (session == null) {
            System.err.println("No session found for channel: " + clientChannel.getRemoteAddress());
            return empty();
        }

        List<Integer> grantedQosLevels = subscribePacket.getSubscriptions().stream()
                .map(subscription -> {
                    session.addSubscription(subscription.topicFilter(), subscription.qos());
                    topicTree.subscribeTo(subscription.topicFilter(), session.getClientId());

                    return subscription.qos().getValue();
                })
                .toList();

        var fixedHeader = new MqttFixedHeader(SUBACK, (byte) 0, 2 + grantedQosLevels.size());
        return withResponse(new SubAckPacket(fixedHeader, subscribePacket.getPacketIdentifier(), grantedQosLevels));
    }
}
