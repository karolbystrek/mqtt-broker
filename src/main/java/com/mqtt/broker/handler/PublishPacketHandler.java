package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.event.PublishEvent;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.pipeline.ProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

import static com.mqtt.broker.packet.MqttPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttPacketType.PUBREC;
import static com.mqtt.broker.pipeline.ProcessingResult.empty;
import static com.mqtt.broker.pipeline.ProcessingResult.withEvent;
import static com.mqtt.broker.pipeline.ProcessingResult.withResponse;
import static com.mqtt.broker.pipeline.ProcessingResult.withResponseAndEvent;

@Slf4j
@RequiredArgsConstructor
class PublishPacketHandler implements PacketHandler<PublishPacket> {

    private final BrokerContext context;

    @Override
    public ProcessingResult handle(SocketChannel clientChannel, PublishPacket packet) {
        // Authorization is now handled by AuthorizationInterceptor
        var event = new PublishEvent(packet);

        return switch (packet.getQosLevel()) {
            case AT_MOST_ONCE -> withEvent(event);

            case AT_LEAST_ONCE -> packet.getPacketIdentifier()
                    .map(packetId -> withResponseAndEvent(createPubAck(packetId), event))
                    .orElse(empty()); // error, qos 1 must have a packet id

            case EXACTLY_ONCE -> packet.getPacketIdentifier()
                    .map(packetId -> {
                        var session = context.getSessionManager().getSession(clientChannel);
                        if (session != null) {
                            session.storeIncomingMessage(packet);
                        }
                        return withResponse(createPubRec(packetId));
                    })
                    .orElse(empty()); // error, qos 2 must have a packet id
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
