package com.mqtt.broker.handler;

import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.event.PublishEvent;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubCompPacket;
import com.mqtt.broker.packet.PubRelPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndEvent;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBCOMP;

@Slf4j
@RequiredArgsConstructor
public class PubRelPacketHandler implements MqttPacketHandler {

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) {
        var pubRelPacket = (PubRelPacket) packet;

        log.info("Handling PUBREL packet: {}", pubRelPacket);

        var session = context.getSession(clientChannel);
        int packetId = pubRelPacket.getPacketIdentifier();

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
