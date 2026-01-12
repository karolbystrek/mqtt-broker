package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.event.PublishEvent;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PublishPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withEvent;
import static com.mqtt.broker.handler.HandlerResult.withResponse;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndEvent;
import static com.mqtt.broker.packet.MqttPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttPacketType.PUBREC;

@Slf4j
@RequiredArgsConstructor
class PublishPacketHandler implements PacketHandler<PublishPacket> {

    private final BrokerContext context;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, PublishPacket packet) {
        var session = context.getSession(clientChannel);
        var topic = packet.variableHeader().topicName();

        if (isAuthorized(session.getUsername(), topic)) {
            return handleAuthorized(clientChannel, packet);
        }

        return handleUnAuthorized(packet, session.getUsername());
    }

    private HandlerResult handleAuthorized(SocketChannel clientChannel, PublishPacket packet) {
        var event = new PublishEvent(packet);

        return switch (packet.getQosLevel()) {
            case AT_MOST_ONCE -> withEvent(event);

            case AT_LEAST_ONCE -> packet.getPacketIdentifier()
                    .map(packetId -> withResponseAndEvent(createPubAck(packetId), event))
                    .orElse(empty()); // error, qos 1 must have a packet id

            case EXACTLY_ONCE -> packet.getPacketIdentifier()
                    .map(packetId -> {
                        context.getSession(clientChannel).storeIncomingMessage(packet);
                        return withResponse(createPubRec(packetId));
                    })
                    .orElse(empty()); // error, qos 2 must have a packet id
        };
    }

    private HandlerResult handleUnAuthorized(PublishPacket packet, String username) {
        log.warn("Unauthorized PUBLISH attempt by user '{}' on topic '{}'", username, packet.variableHeader().topicName());
        return packet.getPacketIdentifier()
                .map(packetId -> switch (packet.getQosLevel()) {
                    case AT_LEAST_ONCE -> withResponse(createPubAck(packetId));
                    case EXACTLY_ONCE -> withResponse(createPubRec(packetId));
                    case AT_MOST_ONCE -> empty();
                }).orElse(empty());
    }

    private boolean isAuthorized(String username, String topic) {
        return context.getAuthorizationService().canPublish(username, topic);
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
