package com.mqtt.broker.event;

import java.nio.channels.SocketChannel;
import java.util.List;

import com.mqtt.broker.Session;
import com.mqtt.broker.context.BrokerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
            case PublishEvent(var channel, var packet) -> context.getMessageDispatcher().dispatch(packet);
        }
    }

    private void handleClientConnected(SocketChannel channel, Session session) {
        log.info("Handling ClientConnectedEvent for client: {}", session.getClientId());
        if (session != null) {
            context.getPendingMessageService().deliverPendingMessages(channel, session);
        }
    }

    private void handleCloseConnection(SocketChannel channel) {
        log.info("Handling CloseConnectionEvent for channel: {}", channel);
        
    }

    private void handleClientSubscribed(SocketChannel channel, List<String> topicFilters) {
        log.info("Handling ClientSubscribedEvent for channel: {}", channel);
        for (String topicFilter : topicFilters) {
            var retainedMessages = context.getTopicTree().getRetainedMessagesMatching(topicFilter);
            for (var retainedMsg : retainedMessages) {
                context.getPacketSender().send(channel, retainedMsg);
            }
        }
    }
}
