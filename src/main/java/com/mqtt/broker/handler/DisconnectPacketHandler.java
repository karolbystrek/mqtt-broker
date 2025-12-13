package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.event.CloseConnectionEvent;
import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.withEvent;

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
            return withEvent(new CloseConnectionEvent(clientChannel));
        }

        String clientId = session.getClientId();

        session.setWillMessage(null); // discard will message

        context.closeSession(clientChannel);
        log.info("Closed session for client: {}", clientId);

        return withEvent(new CloseConnectionEvent(clientChannel));
    }
}
