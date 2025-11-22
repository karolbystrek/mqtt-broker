package com.mqtt.broker.event;

import com.mqtt.broker.Session;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.packet.PublishPacket.PublishVariableHeader;
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
            case PublishEvent(var ignored, var packet) -> handlePublishEvent(packet);
            case ConnectionLostEvent(var ignored, var session) -> handleConnectionLost(session);
        }
    }

    private void handleClientConnected(SocketChannel channel, Session session) {
        log.info("Handling ClientConnectedEvent for client: {}", session.getClientId());
        context.getPendingMessageService().deliverPendingMessages(channel, session);
    }

    private void handleCloseConnection(SocketChannel channel) {
        log.info("Handling CloseConnectionEvent for channel: {}", channel);
    }

    private void handlePublishEvent(PublishPacket packet) {
        log.info("Handling PublishEvent for packet: {}", packet);
        context.getMessageDispatcher().dispatch(packet);
    }

    private void handleClientSubscribed(SocketChannel channel, List<String> topicFilters) {
        log.info("Handling ClientSubscribedEvent for channel: {}", channel);
        Session session = context.getSession(channel);

        for (String topicFilter : topicFilters) {
            var retainedMessages = context.getTopicTree().getRetainedMessagesMatching(topicFilter);
            for (var retainedMsgWithTopic : retainedMessages) {
                var retainedMsg = retainedMsgWithTopic.message();
                String topic = retainedMsgWithTopic.topic();

                byte flags = 1; // Retain = 1
                flags |= (byte) (retainedMsg.qos().getValue() << 1);

                var fixedHeader = new MqttFixedHeader(PUBLISH, flags, 0);

                int packetId = 0;
                if (retainedMsg.qos().getValue() > 0 && session != null) {
                    packetId = session.nextPacketId();
                }

                var variableHeader = new PublishVariableHeader(topic, packetId);
                var packet = new PublishPacket(fixedHeader, variableHeader, retainedMsg.payload());

                context.getPacketSender().send(channel, packet);
            }
        }
    }

    private void handleConnectionLost(Session session) {
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

            var fixedHeader = new MqttFixedHeader(PUBLISH, flags, 0);
            int packetId = 0;
            if (willMessage.qos() > 0) {
                packetId = session.nextPacketId();
            }

            var variableHeader = new PublishVariableHeader(willMessage.topic(), packetId);
            var payload = willMessage.message().getBytes(UTF_8);

            var publishPacket = new PublishPacket(fixedHeader, variableHeader, payload);

            context.getMessageDispatcher().dispatch(publishPacket);
        }
        context.closeSession(session);
    }
}
