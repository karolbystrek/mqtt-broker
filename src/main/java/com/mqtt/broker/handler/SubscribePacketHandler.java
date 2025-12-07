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
import java.util.ArrayList;
import java.util.List;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndEvent;
import static com.mqtt.broker.packet.MqttControlPacketType.SUBACK;

@RequiredArgsConstructor
@Slf4j
public class SubscribePacketHandler implements MqttPacketHandler {

    private static final int FAILURE_CODE = 0x80;

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        if (!(packet instanceof SubscribePacket subscribePacket)) {
            return empty();
        }

        log.info("Received SUBSCRIBE packet: {}", subscribePacket);

        Session session = context.getSession(clientChannel);
        if (session == null) {
            log.error("No session found for channel: {}", clientChannel.getRemoteAddress());
            return empty();
        }

        var grantedQosLevels = new ArrayList<Integer>();
        var grantedTopics = new ArrayList<String>();
        var username = session.getUsername();

        subscribePacket.getSubscriptions().forEach(subscription -> {
            var topic = subscription.topic();
            if (isAuthorized(username, topic)) {
                session.addSubscription(topic, subscription.qos());
                context.getTopicTree().subscribeTo(topic, session.getClientId());

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

        var subAckPacket = createSubAck(subscribePacket.getPacketIdentifier(), grantedQosLevels);
        if (grantedTopics.isEmpty()) {
            return withResponse(subAckPacket);
        }
        var event = new ClientSubscribedEvent(clientChannel, grantedTopics);
        return withResponseAndEvent(subAckPacket, event);
    }

    private boolean isAuthorized(String username, String topic) {
        return context.getUserRegistry().canSubscribe(username, topic);
    }

    private SubAckPacket createSubAck(int packetId, List<Integer> grantedQosLevels) {
        var fixedHeader = new MqttFixedHeader(SUBACK, (byte) 0, 2 + grantedQosLevels.size());
        return new SubAckPacket(fixedHeader, packetId, grantedQosLevels);
    }
}
