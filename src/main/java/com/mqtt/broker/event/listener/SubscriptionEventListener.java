package com.mqtt.broker.event.listener;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.event.BrokerEvent;
import com.mqtt.broker.event.ClientSubscribedEvent;
import com.mqtt.broker.event.EventListener;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.packet.PublishPacket.PublishVariableHeader;
import com.mqtt.broker.trie.TopicPath;
import com.mqtt.broker.trie.strategy.retainedMessage.RetainedMessageWithTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.mqtt.broker.packet.MqttPacketType.PUBLISH;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
public class SubscriptionEventListener implements EventListener {

    private final BrokerContext context;

    @Override
    public void onEvent(BrokerEvent event) {
        if (event instanceof ClientSubscribedEvent(var channel, var topicFilters)) {
            handleClientSubscribed(channel, topicFilters);
        }
    }

    private void handleClientSubscribed(SocketChannel channel, List<String> topicFilters) {
        log.info("Handling ClientSubscribedEvent for channel: {}", channel);
        var session = context.getSessionManager().getSession(channel);

        topicFilters.forEach(topicFilter -> {
            var retainedMessages = new ArrayList<RetainedMessageWithTopic>();
            context.getRetainedMessageRepository().find(TopicPath.parse(topicFilter), retainedMessages);

            retainedMessages.forEach(retainedMessageWithTopic -> {
                var retainedMessage = retainedMessageWithTopic.message();
                String topic = retainedMessageWithTopic.topic();

                byte flags = 1; // Retain = 1
                flags |= (byte) (retainedMessage.qos().getValue() << 1);

                int variableHeaderLength = 2 + topic.getBytes(UTF_8).length;
                if (retainedMessage.qos().getValue() > 0) {
                    variableHeaderLength += 2;
                }
                int remainingLength = variableHeaderLength + retainedMessage.payload().length;

                var fixedHeader = new MqttFixedHeader(PUBLISH, flags, remainingLength);

                int packetId = 0;
                if (retainedMessage.qos().getValue() > 0 && session != null) {
                    packetId = session.nextPacketId();
                }

                var variableHeader = new PublishVariableHeader(topic, packetId);
                var packet = new PublishPacket(fixedHeader, variableHeader, retainedMessage.payload());

                context.getMessageDeliveryService().send(channel, packet);
            });
        });
    }
}
