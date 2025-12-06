package com.mqtt.broker.persistence.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.mqtt.broker.packet.MqttQoS;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public record SessionDTO(
        String clientId,
        Map<String, MqttQoS> subscriptions,
        List<PublishPacketDTO> pendingMessages
) {
    @JsonCreator
    public SessionDTO {
        subscriptions = subscriptions != null ? subscriptions : emptyMap();
        pendingMessages = pendingMessages != null ? pendingMessages : emptyList();
    }
}
