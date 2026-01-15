package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.interceptor.ProcessingResult;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.UnsubAckPacket;
import com.mqtt.broker.packet.UnsubscribePacket;
import com.mqtt.broker.trie.TopicPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.interceptor.ProcessingResult.empty;
import static com.mqtt.broker.interceptor.ProcessingResult.withResponse;
import static com.mqtt.broker.packet.MqttPacketType.UNSUBACK;

@RequiredArgsConstructor
@Slf4j
class UnsubscribePacketHandler implements PacketHandler<UnsubscribePacket> {

    private final BrokerContext context;

    @Override
    public ProcessingResult handle(SocketChannel clientChannel, UnsubscribePacket packet) throws IOException {
        var clientSession = context.getSession(clientChannel);
        if (clientSession == null) {
            log.error("No session found for channel: {}", clientChannel.getRemoteAddress());
            return empty();
        }

        packet.topicFilters().forEach(topicFilter -> {
            clientSession.removeSubscription(topicFilter);

            context.getSubscriptionRepository().remove(clientSession.getClientId(), TopicPath.parse(topicFilter));
        });

        var unsubAckFixedHeader = new MqttFixedHeader(UNSUBACK, (byte) 0, 2);
        return withResponse(new UnsubAckPacket(unsubAckFixedHeader, packet.packetIdentifier()));
    }
}
