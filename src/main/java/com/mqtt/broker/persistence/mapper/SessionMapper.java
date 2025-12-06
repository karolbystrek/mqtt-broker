package com.mqtt.broker.persistence.mapper;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.packet.PublishPacket.PublishVariableHeader;
import com.mqtt.broker.persistence.dto.PublishPacketDTO;
import com.mqtt.broker.persistence.dto.SessionDTO;

import java.util.List;

import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;
import static java.util.Collections.emptyList;

public final class SessionMapper {

    private SessionMapper() {
    }

    public static SessionDTO toDto(Session session) {
        var pendingMessageDTOs = session.getPendingMessages().stream()
                .map(SessionMapper::toPublishPacketDto)
                .toList();

        return new SessionDTO(
                session.getClientId(),
                session.getSubscriptions(),
                pendingMessageDTOs
        );
    }

    public static Session fromDto(SessionDTO dto) {
        List<PublishPacket> pendingMessages =
                dto.pendingMessages() != null ?
                        dto.pendingMessages().stream()
                                .map(SessionMapper::fromPublishPacketDto)
                                .toList()
                        : emptyList();

        return new Session(
                dto.clientId(),
                false, // Persisted sessions are always non-clean sessions
                0,     // Keep alive will be updated on reconnect
                dto.subscriptions(),
                pendingMessages
        );
    }

    private static PublishPacketDTO toPublishPacketDto(PublishPacket packet) {
        return new PublishPacketDTO(
                packet.getVariableHeader().topicName(),
                packet.getVariableHeader().packetIdentifier(),
                packet.getPayload(),
                packet.getQosLevel(),
                packet.isRetain(),
                packet.isDup()
        );
    }

    private static PublishPacket fromPublishPacketDto(PublishPacketDTO dto) {
        byte flags = buildPublishFlags(dto);
        int remainingLength = calculateRemainingLength(dto);

        var fixedHeader = new MqttFixedHeader(PUBLISH, flags, remainingLength);
        var variableHeader = new PublishVariableHeader(dto.topicName(), dto.packetIdentifier());

        return new PublishPacket(fixedHeader, variableHeader, dto.payload());
    }

    private static byte buildPublishFlags(PublishPacketDTO dto) {
        byte flags = 0;
        if (dto.dup()) {
            flags |= 0b0000_1000;
        }
        flags |= (byte) (dto.qos().getValue() << 1);
        if (dto.retain()) {
            flags |= 0b0000_0001;
        }
        return flags;
    }

    private static int calculateRemainingLength(PublishPacketDTO dto) {
        int topicLength = 2 + dto.topicName().length();
        int packetIdLength = dto.qos().requiresPacketId() ? 2 : 0;
        int payloadLength = dto.payload() != null ? dto.payload().length : 0;
        return topicLength + packetIdLength + payloadLength;
    }
}
