package com.mqtt.broker.event.listener;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.event.BrokerEvent;
import com.mqtt.broker.event.ClientConnectedEvent;
import com.mqtt.broker.event.CloseConnectionEvent;
import com.mqtt.broker.event.ConnectionLostEvent;
import com.mqtt.broker.event.EventListener;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.packet.PublishPacket.PublishVariableHeader;
import com.mqtt.broker.session.Session;
import com.mqtt.broker.trie.TopicPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.packet.MqttPacketType.PUBLISH;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@RequiredArgsConstructor
public class ConnectionEventListener implements EventListener {

    private final BrokerContext context;

    @Override
    public void onEvent(BrokerEvent event) {
        switch (event) {
            case ClientConnectedEvent(var channel, var session) -> handleClientConnected(channel, session);
            case CloseConnectionEvent(var channel) -> handleCloseConnection(channel);
            case ConnectionLostEvent(var channel) -> handleConnectionLost(channel);
            default -> {
            }
        }
    }

    private void handleClientConnected(SocketChannel channel, Session session) {
        log.info("Handling ClientConnectedEvent for client: {}", session.getClientId());
        context.getMessageDeliveryService().dispatchPendingMessages(channel, session);
        session.getSubscriptions().forEach((topic, qos) -> {
            context.getSubscriptionRepository().add(session.getClientId(), TopicPath.parse(topic));
        });
    }

    private void handleCloseConnection(SocketChannel channel) {
        log.info("Handling CloseConnectionEvent for channel: {}", channel);
    }

    private void handleConnectionLost(SocketChannel channel) {
        var session = context.getSessionManager().getSession(channel);
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
        context.getSessionManager().closeSession(channel);
    }
}
