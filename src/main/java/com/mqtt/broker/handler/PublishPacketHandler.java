package com.mqtt.broker.handler;

import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.event.PublishEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withEvent;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndEvent;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREC;

@RequiredArgsConstructor
@Slf4j
public class PublishPacketHandler implements MqttPacketHandler {

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) {
        if (!(packet instanceof PublishPacket publishPacket)) {
            return empty();
        }

        log.info("Handling PUBLISH packet: {}", publishPacket);

        var session = context.getSession(clientChannel);

        return switch (publishPacket.getQosLevel()) {
            case AT_LEAST_ONCE -> publishPacket.getPacketIdentifier()
                    .map(packetId -> {
                        var pubAck = createPubAck(packetId);
                        var event = new PublishEvent(clientChannel, publishPacket);
                        return withResponseAndEvent(pubAck, event);
                    })
                    .orElse(empty());
            case EXACTLY_ONCE -> publishPacket.getPacketIdentifier()
                    .map(packetId -> {
                        session.storeIncomingMessage(publishPacket);
                        return withResponse(createPubRec(packetId));
                    })
                    .orElse(empty());
            case AT_MOST_ONCE -> withEvent(new PublishEvent(clientChannel, publishPacket));
        };
    }

    private PubAckPacket createPubAck(int packetId) {
        var fixedHeader = new MqttFixedHeader(PUBACK, (byte) 0, 2);
        return new PubAckPacket(fixedHeader, packetId);
    }

    private PubRecPacket createPubRec(int packetId) {
        var fixedHeader = new MqttFixedHeader(PUBREC, (byte) 0x0, 2);
        return new PubRecPacket(fixedHeader, packetId);
    }
}
