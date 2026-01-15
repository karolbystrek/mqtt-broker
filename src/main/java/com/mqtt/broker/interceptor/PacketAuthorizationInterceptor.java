package com.mqtt.broker.interceptor;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PublishPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;
import java.util.Optional;

import static com.mqtt.broker.interceptor.ProcessingResult.empty;
import static com.mqtt.broker.interceptor.ProcessingResult.withResponse;
import static com.mqtt.broker.packet.MqttPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttPacketType.PUBREC;

@Slf4j
@RequiredArgsConstructor
public class PacketAuthorizationInterceptor extends ChainablePacketInterceptor {

    private final BrokerContext context;

    @Override
    protected Optional<ProcessingResult> process(SocketChannel channel, MqttPacket packet) {
        if (packet instanceof PublishPacket publishPacket) {
            return handlePublishAuthorization(channel, publishPacket);
        }
        return Optional.empty(); // Proceed for other packet types
    }

    private Optional<ProcessingResult> handlePublishAuthorization(SocketChannel channel, PublishPacket packet) {
        var session = context.getSession(channel);
        if (session == null) {
            return Optional.empty();
        }

        String username = session.getUsername();
        String topic = packet.variableHeader().topicName();

        if (context.getAuthorizationService().canPublish(username, topic)) {
            return Optional.empty(); // Authorized, proceed
        }

        log.warn("Unauthorized PUBLISH attempt by user '{}' on topic '{}'", username, topic);

        ProcessingResult result = packet.getPacketIdentifier()
                .map(packetId -> switch (packet.getQosLevel()) {
                    case AT_LEAST_ONCE -> withResponse(createPubAck(packetId));
                    case EXACTLY_ONCE -> withResponse(createPubRec(packetId));
                    case AT_MOST_ONCE -> empty();
                }).orElse(empty());

        return Optional.of(result);
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
