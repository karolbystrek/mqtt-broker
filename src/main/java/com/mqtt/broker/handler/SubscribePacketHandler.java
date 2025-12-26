package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.Session;
import com.mqtt.broker.event.ClientSubscribedEvent;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.SubAckPacket;
import com.mqtt.broker.packet.SubscribePacket;
import com.mqtt.broker.trie.visitor.SubscriptionAddVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndEvent;
import static com.mqtt.broker.packet.MqttPacketType.SUBACK;

@RequiredArgsConstructor
@Slf4j
class SubscribePacketHandler implements PacketHandler<SubscribePacket> {

    private static final int FAILURE_CODE = 0x80;

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, SubscribePacket packet) throws IOException {
        Session session = context.getSession(clientChannel);
        if (session == null) {
            log.error("No session found for channel: {}", clientChannel.getRemoteAddress());
            return empty();
        }

        var grantedQosLevels = new ArrayList<Integer>();
        var grantedTopics = new ArrayList<String>();
        var username = session.getUsername();

        packet.getSubscriptions().forEach(subscription -> {
            var topic = subscription.topic();
            if (isAuthorized(username, topic)) {
                session.addSubscription(topic, subscription.qos());

                String[] levels = topic.split("/");
                var visitor = new SubscriptionAddVisitor(levels, session.getClientId());
                context.getSubscriptionTree().accept(visitor);

                grantedQosLevels.add(subscription.qos().getValue());
                grantedTopics.add(topic);
                log.info("Client '{}' subscribed to topic '{}' with QoS {}.",
                        session.getClientId(), topic, subscription.qos().getValue());
            } else {
                log.warn("Client '{}' is not authorized to subscribe to topic '{}'.",
                        session.getClientId(), topic);
                grantedQosLevels.add(FAILURE_CODE);
            }
        });

        var subAckPacket = createSubAck(packet.getPacketIdentifier(), grantedQosLevels);
        if (grantedTopics.isEmpty()) {
            return withResponse(subAckPacket);
        }
        var event = new ClientSubscribedEvent(clientChannel, grantedTopics);
        return withResponseAndEvent(subAckPacket, event);
    }

    private boolean isAuthorized(String username, String topic) {
        return context.getAuthorizationService().canSubscribe(username, topic);
    }

    private SubAckPacket createSubAck(int packetId, List<Integer> grantedQosLevels) {
        var fixedHeader = new MqttFixedHeader(SUBACK, (byte) 0, 2 + grantedQosLevels.size());
        return new SubAckPacket(fixedHeader, packetId, grantedQosLevels);
    }
}
