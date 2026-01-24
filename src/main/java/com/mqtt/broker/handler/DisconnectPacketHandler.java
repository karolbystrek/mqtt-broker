package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.event.CloseConnectionEvent;
import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.pipeline.ProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.pipeline.ProcessingResult.withEvent;

@Slf4j
@RequiredArgsConstructor
class DisconnectPacketHandler implements PacketHandler<DisconnectPacket> {

    private final BrokerContext context;

    @Override
    public ProcessingResult handle(SocketChannel clientChannel, DisconnectPacket packet) throws IOException {
        var session = context.getSessionManager().getSession(clientChannel);
        if (session == null) {
            log.warn("No active session found for disconnecting client");
            return withEvent(new CloseConnectionEvent(clientChannel));
        }

        String clientId = session.getClientId();

        session.setWillMessage(null); // discard will value

        context.getSessionManager().closeSession(clientChannel);
        log.info("Closed session for client: {}", clientId);

        return withEvent(new CloseConnectionEvent(clientChannel));
    }
}
