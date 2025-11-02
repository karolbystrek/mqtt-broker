package com.mqtt.broker.packet;

import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Getter
@ToString
public final class PublishPacket extends MqttPacket {

    private final PublishVariableHeader variableHeader;
    private final byte[] payload; // payload is raw bytes as it can be anything


    public PublishPacket(MqttFixedHeader fixedHeader, PublishVariableHeader variableHeader, byte[] payload) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PUBLISH) {
            throw invalidPacketType(PublishPacket.class);
        }
        if (variableHeader.topicName == null || variableHeader.topicName.isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        if (getQosLevel().requiresPacketId() && variableHeader.packetIdentifier <= 0) {
            throw new IllegalArgumentException("Packet Identifier must be greater than 0 for QoS levels 1 and 2");
        }
        if (variableHeader.packetIdentifier() < 0 || variableHeader.packetIdentifier() > 65535) {
            throw invalidPacketIdentifier();
        }
        this.variableHeader = variableHeader;
        this.payload = payload != null ? payload.clone() : new byte[0];
    }

    public boolean isDup() {
        return (getFixedHeader().flags() & 0b0000_1000) != 0;
    }

    public MqttQoS getQosLevel() {
        int qosValue = (getFixedHeader().flags() & 0b0000_0110) >> 1;
        return MqttQoS.fromInt(qosValue);
    }

    public boolean isRetain() {
        return (getFixedHeader().flags() & 0b0000_0001) != 0;
    }

    public Optional<Integer> getPacketIdentifier() {
        if (getQosLevel().requiresPacketId()) {
            return of(variableHeader.packetIdentifier);
        }
        return empty();
    }

    public record PublishVariableHeader(
            String topicName,
            int packetIdentifier // Optional, only present for QoS levels 1 and 2
    ) {
    }
}
