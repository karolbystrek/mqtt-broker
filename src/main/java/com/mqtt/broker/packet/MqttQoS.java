package com.mqtt.broker.packet;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MqttQoS {

    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2);

    private final int value;

    MqttQoS(int value) {
        this.value = value;
    }

    public static MqttQoS fromInt(int value) {
        return Arrays.stream(values())
                .filter(qos -> qos.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid QoS value: " + value));
    }

    public boolean requiresPacketId() {
        return this == AT_LEAST_ONCE || this == EXACTLY_ONCE;
    }
}
