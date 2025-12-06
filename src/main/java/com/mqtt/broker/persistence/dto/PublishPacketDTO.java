package com.mqtt.broker.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.mqtt.broker.packet.MqttQoS;

public record PublishPacketDTO(
        String topicName,
        int packetIdentifier,
        byte[] payload,
        MqttQoS qos,
        boolean retain,
        boolean dup
) {
    @JsonCreator
    public PublishPacketDTO {
    }
}
