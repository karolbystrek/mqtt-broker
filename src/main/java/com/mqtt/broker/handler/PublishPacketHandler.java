package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.trie.TopicTree;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

import static com.mqtt.broker.handler.HandlerResult.empty;
import static com.mqtt.broker.handler.HandlerResult.withAction;
import static com.mqtt.broker.handler.HandlerResult.withResponseAndAction;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREC;
import static com.mqtt.broker.packet.MqttQoS.AT_MOST_ONCE;

@RequiredArgsConstructor
public class PublishPacketHandler implements MqttPacketHandler {

    private final MqttPacketEncoder encoder = new MqttPacketEncoder();
    private final TopicTree topicTree;
    private final Map<String, SocketChannel> clientIdToChannel;
    private final Map<String, Session> persistentSessions;

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) {
        if (!(packet instanceof PublishPacket publishPacket)) {
            return empty();
        }

        System.out.println("Handling PUBLISH packet: " + publishPacket);

        PostConnectionAction forwardAction = channel -> forwardToSubscribers(publishPacket);

        return switch (publishPacket.getQosLevel()) {
            case AT_LEAST_ONCE -> publishPacket.getPacketIdentifier()
                    .map(packetId -> withResponseAndAction(createPubAck(packetId), forwardAction))
                    .orElse(withAction(forwardAction));
            case EXACTLY_ONCE -> publishPacket.getPacketIdentifier()
                    .map(packetId -> withResponseAndAction(createPubRec(packetId), forwardAction))
                    .orElse(withAction(forwardAction));
            default -> withAction(forwardAction); // QoS 0 requires no response but still forwards
        };
    }

    private void forwardToSubscribers(PublishPacket packet) {
        var topic = packet.getVariableHeader().topicName();
        var subscribedClientIds = topicTree.getSubscribersFor(topic);

        if (subscribedClientIds.isEmpty()) {
            return;
        }

        var encodedPacket = encoder.encode(packet);

        subscribedClientIds.forEach(clientId ->
                routeMessageToClient(clientId, packet, encodedPacket)
        );
    }

    private void routeMessageToClient(String clientId, PublishPacket packet, ByteBuffer encodedPacket) {
        SocketChannel channel = clientIdToChannel.get(clientId);

        if (channel != null) {
            sendPacketToOnlineClient(channel, encodedPacket);
        } else {
            queueMessageForOfflineClient(clientId, packet);
        }
    }

    private void sendPacketToOnlineClient(SocketChannel channel, ByteBuffer encodedPacket) {
        try {
            var buffer = encodedPacket.duplicate();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        } catch (IOException e) {
            System.err.println("Failed to send PUBLISH packet to online client: " + e.getMessage());
        }
    }

    private void queueMessageForOfflineClient(String clientId, PublishPacket packet) {
        Session persistentSession = persistentSessions.get(clientId);

        if (persistentSession == null) {
            return;
        }

        if (packet.getQosLevel() != AT_MOST_ONCE) { // qos 1 and 2 should be queued
            System.out.println("Queuing PUBLISH packet for offline client: " + clientId);
            persistentSession.enqueuePendingMessage(packet);
        }
    }

    private PubAckPacket createPubAck(int packetId) {
        var fixedHeader = new MqttFixedHeader(PUBACK, (byte) 0, 2);
        return new PubAckPacket(fixedHeader, packetId);
    }

    private PubRecPacket createPubRec(int packetId) {
        var fixedHeader = new MqttFixedHeader(PUBREC, (byte) 0, 2);
        return new PubRecPacket(fixedHeader, packetId);
    }
}
