package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.SubAckPacket;
import com.mqtt.broker.packet.SubscribePacket;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

import static com.mqtt.broker.handler.HandlerResult.empty;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mqtt.broker.handler.HandlerResult.withResponse;
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

        List<Integer> grantedQosLevels = subscribePacket.getSubscriptions().stream()
                .map(subscription -> {
                    session.addSubscription(subscription.topicFilter(), subscription.qos());
                    context.getTopicTree().subscribeTo(subscription.topicFilter(), session.getClientId());

                    return subscription.qos().getValue();
                })
                .toList();

        var fixedHeader = new MqttFixedHeader(SUBACK, (byte) 0, 2 + grantedQosLevels.size());
        return withResponse(new SubAckPacket(fixedHeader, subscribePacket.getPacketIdentifier(), grantedQosLevels));
    }
}
