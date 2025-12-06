package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.event.ClientSubscribedEvent;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.SubAckPacket;
import com.mqtt.broker.packet.SubscribePacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndEvent;
import static com.mqtt.broker.packet.MqttControlPacketType.SUBACK;

@RequiredArgsConstructor
@Slf4j
public class SubscribePacketHandler implements MqttPacketHandler {

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        var subscribePacket = (SubscribePacket) packet;

        log.info("Received SUBSCRIBE packet: {}", subscribePacket);

        Session session = context.getSession(clientChannel);
        if (session == null) {
            log.error("No session found for channel: {}", clientChannel.getRemoteAddress());
            return empty();
        }

        String username = session.getUsername();
        List<Integer> grantedQosLevels = subscribePacket.getSubscriptions().stream()
                .map(subscription -> {
                    if (!context.getUserRegistry().canSubscribe(username, subscription.topic())) {
                        log.warn("Client '{}' is not authorized to subscribe to topic '{}'.",
                                session.getClientId(), subscription.topic());
                        return -1;
                    }
                    session.addSubscription(subscription.topic(), subscription.qos());
                    context.getTopicTree().subscribeTo(subscription.topic(), session.getClientId());

                    return subscription.qos().getValue();
                })
                .toList();

        var fixedHeader = new MqttFixedHeader(SUBACK, (byte) 0, 2 + grantedQosLevels.size());
        var subAckPacket = new SubAckPacket(fixedHeader, subscribePacket.getPacketIdentifier(), grantedQosLevels);

        List<String> topicFilters = subscribePacket.getSubscriptions().stream()
                .map(SubscribePacket.Subscription::topic)
                .toList();

        var event = new ClientSubscribedEvent(clientChannel, topicFilters);
        return withResponseAndEvent(subAckPacket, event);
    }
}
