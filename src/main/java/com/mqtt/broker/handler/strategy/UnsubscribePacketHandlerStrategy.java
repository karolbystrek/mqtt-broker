package com.mqtt.broker.handler.strategy;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.handler.HandlerResult;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.UnsubAckPacket;
import com.mqtt.broker.packet.UnsubscribePacket;
import com.mqtt.broker.trie.visitor.SubscriptionRemoveVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.packet.MqttControlPacketType.UNSUBACK;

@RequiredArgsConstructor
@Slf4j
public class UnsubscribePacketHandlerStrategy implements PacketHandlerStrategy<UnsubscribePacket> {

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, UnsubscribePacket packet) throws IOException {
        var clientSession = context.getSession(clientChannel);
        if (clientSession == null) {
            log.error("No session found for channel: {}", clientChannel.getRemoteAddress());
            return empty();
        }

        packet.getTopicFilters().forEach(topicFilter -> {
            clientSession.removeSubscription(topicFilter);

            String[] levels = topicFilter.split("/");
            var visitor = new SubscriptionRemoveVisitor(levels, clientSession.getClientId());
            context.getSubscriptionTree().accept(visitor);
        });

        var unsubAckFixedHeader = new MqttFixedHeader(UNSUBACK, (byte) 0, 2);
        return withResponse(new UnsubAckPacket(unsubAckFixedHeader, packet.getPacketIdentifier()));
    }
}
