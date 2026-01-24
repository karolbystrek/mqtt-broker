package com.mqtt.broker.packet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

import static com.mqtt.broker.exception.InvalidPacketIdentifierException.invalidPacketIdentifier;
import static com.mqtt.broker.exception.InvalidPacketTypeException.invalidPacketType;
import static com.mqtt.broker.packet.MqttPacketType.PUBLISH;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public record PublishPacket(
        MqttFixedHeader fixedHeader,
        PublishVariableHeader variableHeader,
        byte[] payload
) implements MqttPacket {

    private static final int DUP_MASK = 0b0000_1000;
    private static final int QOS_MASK = 0b0000_0110;
    private static final int QOS_SHIFT = 1;
    private static final int RETAIN_MASK = 0b0000_0001;

    public PublishPacket {
        if (fixedHeader.packetType() != PUBLISH) {
            throw invalidPacketType(PublishPacket.class);
        }
        if (variableHeader.topicName == null || variableHeader.topicName.isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        if (variableHeader.packetIdentifier < 0 || variableHeader.packetIdentifier > 65535) {
            throw invalidPacketIdentifier();
        }

        int qosValue = (fixedHeader.flags() & QOS_MASK) >> QOS_SHIFT;
        MqttQoS qos = MqttQoS.fromInt(qosValue);
        if (qos.requiresPacketId() && variableHeader.packetIdentifier <= 0) {
            throw new IllegalArgumentException("Packet Identifier must be greater than 0 for QoS levels 1 and 2");
        }

        payload = payload != null ? payload.clone() : new byte[0];
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }

    public boolean isDup() {
        return (fixedHeader.flags() & DUP_MASK) != 0;
    }

    public MqttQoS getQosLevel() {
        int qosValue = (fixedHeader.flags() & QOS_MASK) >> QOS_SHIFT;
        return MqttQoS.fromInt(qosValue);
    }

    public boolean isRetain() {
        return (fixedHeader.flags() & RETAIN_MASK) != 0;
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

    @JsonCreator
    public static PublishPacket fromJson(@JsonProperty("fixedHeader") MqttFixedHeader fixedHeader,
                                         @JsonProperty("variableHeader") PublishVariableHeader variableHeader,
                                         @JsonProperty("payload") byte[] payload) {
        return new PublishPacket(fixedHeader, variableHeader, payload);
    }
}