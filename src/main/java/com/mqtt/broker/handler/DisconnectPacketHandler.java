package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withAction;

@RequiredArgsConstructor
@Slf4j
public final class DisconnectPacketHandler implements MqttPacketHandler {

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        var disconnectPacket = (DisconnectPacket) packet;

        log.info("Received DISCONNECT packet: {}", disconnectPacket);

        Session session = context.getSession(clientChannel);
        if (session == null) {
            log.warn("No active session found for disconnecting client");
            return withAction(java.nio.channels.SocketChannel::close);
        }

        String clientId = session.getClientId();
        if (session.isCleanSession()) {
            context.getTopicTree().removeAllSubscriptionsFor(clientId);
        } else {
            context.savePersistentSession(clientId, session);
            log.info("Saved persistent session for client: {}", clientId);
        }

        context.removeSession(clientChannel);

        return empty();
    }
}
