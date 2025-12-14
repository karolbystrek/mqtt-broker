package com.mqtt.broker.event;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.Session;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.packet.PublishPacket.PublishVariableHeader;
import com.mqtt.broker.trie.RetainedMessageWithTopic;
import com.mqtt.broker.trie.visitor.RetainedMessageFinderVisitor;
import com.mqtt.broker.trie.visitor.SubscriptionAddVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;
import java.util.List;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
public class BrokerEventListener implements EventListener {

    private final BrokerContext context;

    @Override
    public void onEvent(BrokerEvent event) {
        switch (event) {
            case ClientConnectedEvent(var channel, var session) -> handleClientConnected(channel, session);
            case ClientSubscribedEvent(var channel, var topicFilters) -> handleClientSubscribed(channel, topicFilters);
            case CloseConnectionEvent(var channel) -> handleCloseConnection(channel);
            case PublishEvent(var packet) -> handlePublishEvent(packet);
            case ConnectionLostEvent(var channel) -> handleConnectionLost(channel);
        }
    }

    private void handleClientConnected(SocketChannel channel, Session session) {
        log.info("Handling ClientConnectedEvent for client: {}", session.getClientId());
        context.getMessageDeliveryService().dispatchPendingMessages(channel, session);
        session.getSubscriptions().forEach((topic, qos) -> {
            String[] levels = topic.split("/");
            var visitor = new SubscriptionAddVisitor(levels, session.getClientId());
            context.getSubscriptionTree().accept(visitor);
        });
    }

    private void handleCloseConnection(SocketChannel channel) {
        log.info("Handling CloseConnectionEvent for channel: {}", channel);
    }

    private void handlePublishEvent(PublishPacket packet) {
        log.info("Handling PublishEvent for packet: {}", packet);
        context.getMessageDeliveryService().dispatch(packet);
    }

    private void handleClientSubscribed(SocketChannel channel, List<String> topicFilters) {
        log.info("Handling ClientSubscribedEvent for channel: {}", channel);
        Session session = context.getSession(channel);

        for (String topicFilter : topicFilters) {
            var retainedMessages = new java.util.ArrayList<RetainedMessageWithTopic>();
            String[] levels = topicFilter.split("/");
            var visitor = new RetainedMessageFinderVisitor(levels, retainedMessages);
            context.getRetainedMessageTree().accept(visitor);

            for (var retainedMsgWithTopic : retainedMessages) {
                var retainedMsg = retainedMsgWithTopic.message();
                String topic = retainedMsgWithTopic.topic();

                byte flags = 1; // Retain = 1
                flags |= (byte) (retainedMsg.qos().getValue() << 1);

                int variableHeaderLength = 2 + topic.getBytes(UTF_8).length;
                if (retainedMsg.qos().getValue() > 0) {
                    variableHeaderLength += 2;
                }
                int remainingLength = variableHeaderLength + retainedMsg.payload().length;

                var fixedHeader = new MqttFixedHeader(PUBLISH, flags, remainingLength);

                int packetId = 0;
                if (retainedMsg.qos().getValue() > 0 && session != null) {
                    packetId = session.nextPacketId();
                }

                var variableHeader = new PublishVariableHeader(topic, packetId);
                var packet = new PublishPacket(fixedHeader, variableHeader, retainedMsg.payload());

                context.getMessageDeliveryService().send(channel, packet);
            }
        }
    }

    private void handleConnectionLost(SocketChannel channel) {
        Session session = context.getSession(channel);
        if (session == null) {
            return;
        }

        if (session.getWillMessage() != null) {
            log.info("Client {} disconnected unexpectedly. Sending Will Message.", session.getClientId());
            var willMessage = session.getWillMessage();

            byte flags = 0;
            flags |= (byte) (willMessage.qos() << 1);
            if (willMessage.retain()) {
                flags |= 1;
            }

            var payload = willMessage.message().getBytes(UTF_8);
            int variableHeaderLength = 2 + willMessage.topic().getBytes(UTF_8).length;
            if (willMessage.qos() > 0) {
                variableHeaderLength += 2;
            }
            int remainingLength = variableHeaderLength + payload.length;

            var fixedHeader = new MqttFixedHeader(PUBLISH, flags, remainingLength);
            int packetId = 0;
            if (willMessage.qos() > 0) {
                packetId = session.nextPacketId();
            }

            var variableHeader = new PublishVariableHeader(willMessage.topic(), packetId);

            var publishPacket = new PublishPacket(fixedHeader, variableHeader, payload);

            context.getMessageDeliveryService().dispatch(publishPacket);
        }
        context.closeSession(channel);
    }
}
