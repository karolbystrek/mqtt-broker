package com.mqtt.broker;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Getter
public enum MqttQoS {

    AT_MOST_ONCE(0),
    AT_LEAST_ONCE(1),
    EXACTLY_ONCE(2);

    private final int value;

    MqttQoS(int value) {
        this.value = value;
    }

    private static final Map<Integer, MqttQoS> valueToQoSMap = Arrays
            .stream(values())
            .collect(toMap(MqttQoS::getValue, identity()));

    public static MqttQoS fromInt(int value) {
        MqttQoS qos = valueToQoSMap.get(value);
        if (qos == null) {
            throw new IllegalArgumentException("Invalid MQTT QoS value: " + value);
        }
        return qos;
    }
}
