package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.UnsubAckPacket;
import com.mqtt.broker.packet.UnsubscribePacket;
import com.mqtt.broker.trie.TopicTree;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

import static com.mqtt.broker.packet.MqttControlPacketType.UNSUBACK;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@RequiredArgsConstructor
public class UnsubscribePacketHandler implements MqttPacketHandler {

    private final Map<SocketChannel, Session> activeSessions;
    private final TopicTree topicTree;

    @Override
    public Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof UnsubscribePacket unsubscribePacket)) {
            return empty();
        }

        System.out.println("Handling UNSUBSCRIBE packet: " + unsubscribePacket);

        var clientSession = activeSessions.get(clientChannel);
        if (clientSession == null) {
            System.err.println("No session found for channel: " + clientChannel.getRemoteAddress());
            return empty();
        }

        unsubscribePacket.getTopicFilters().forEach(topicFilter -> {
            clientSession.removeSubscription(topicFilter);
            topicTree.unsubscribe(topicFilter, clientSession.getClientId());
        });

        var unsubAckFixedHeader = new MqttFixedHeader(UNSUBACK, (byte) 0, 2);
        return of(new UnsubAckPacket(unsubAckFixedHeader, unsubscribePacket.getPacketIdentifier()));
    }
}
