package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.event.PublishEvent;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubCompPacket;
import com.mqtt.broker.packet.PubRelPacket;
import com.mqtt.broker.pipeline.ProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.packet.MqttPacketType.PUBCOMP;
import static com.mqtt.broker.pipeline.ProcessingResult.withResponse;
import static com.mqtt.broker.pipeline.ProcessingResult.withResponseAndEvent;

@Slf4j
@RequiredArgsConstructor
class PubRelPacketHandler implements PacketHandler<PubRelPacket> {

    private final BrokerContext context;

    @Override
    public ProcessingResult handle(SocketChannel clientChannel, PubRelPacket packet) {
        var session = context.getSessionManager().getSession(clientChannel);
        int packetId = packet.packetIdentifier();

        var message = session.retrieveIncomingMessage(packetId);
        var fixedHeader = new MqttFixedHeader(PUBCOMP, (byte) 0, 2);
        var pubComp = new PubCompPacket(fixedHeader, packetId);

        if (message != null) {
            var event = new PublishEvent(message);
            return withResponseAndEvent(pubComp, event);
        } else {
            log.warn("Received PUBREL for unknown packet ID: {}", packetId);
            return withResponse(pubComp);
        }
    }
}
