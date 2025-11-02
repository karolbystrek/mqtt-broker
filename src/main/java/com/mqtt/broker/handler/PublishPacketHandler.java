package com.mqtt.broker.handler;

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
import java.util.Objects;
import java.util.Optional;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREC;
import static java.util.Optional.empty;

@RequiredArgsConstructor
public class PublishPacketHandler implements MqttPacketHandler {

    private final MqttPacketEncoder encoder = new MqttPacketEncoder();
    private final TopicTree topicTree;
    private final Map<String, SocketChannel> clientIdToChannel;

    @Override
    public Optional<MqttPacket> handle(SocketChannel clientChannel, MqttPacket packet) {
        if (!(packet instanceof PublishPacket publishPacket)) {
            return empty();
        }

        System.out.println("Handling PUBLISH packet: " + publishPacket);

        forwardToSubscribers(publishPacket);

        return switch (publishPacket.getQosLevel()) {
            case AT_LEAST_ONCE -> publishPacket.getPacketIdentifier()
                    .map(packetId -> createPubAck(packetId));
            case EXACTLY_ONCE -> publishPacket.getPacketIdentifier()
                    .map(packetId -> createPubRec(packetId));
            default -> empty(); // QoS 0 requires no response
        };
    }

    private void forwardToSubscribers(PublishPacket packet) {
        var topic = packet.getVariableHeader().topicName();
        var subscribedClientIds = topicTree.getSubscribersFor(topic);

        if (subscribedClientIds.isEmpty()) {
            return;
        }

        var encodedPacket = encodePacket(packet); // encode once for all subscribers

        subscribedClientIds.stream()
                .map(clientIdToChannel::get)
                .filter(Objects::nonNull)
                .forEach(channel -> sendPacket(channel, encodedPacket));
    }

    private ByteBuffer encodePacket(PublishPacket packet) {
        var buffer = encoder.encode(packet);
        buffer.flip();
        return buffer;
    }

    private void sendPacket(SocketChannel channel, ByteBuffer packetBuffer) {
        try {
            // Duplicate the buffer so each client gets independent position tracking
            var buffer = packetBuffer.duplicate();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        } catch (IOException e) {
            System.err.println("Failed to send PUBLISH packet to client: " + e.getMessage());
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
