package com.mqtt.broker;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Getter
public enum MqttControlPacketType {
    CONNECT(1), // Client request to connect to Broker
    CONNACK(2), // Connect Acknowledgment
    PUBLISH(3), // Publish message
    PUBACK(4), // Publish Acknowledgment
    PUBREC(5), // Publish Received (assured delivery part 1)
    PUBREL(6), // Publish Release (assured delivery part 2)
    PUBCOMP(7), // Publish Complete (assured delivery part 3)
    SUBSCRIBE(8), // Client Subscribe request
    SUBACK(9), // Subscribe Acknowledgment
    UNSUBSCRIBE(10), // Client Unsubscribe request
    UNSUBACK(11), // Unsubscribe Acknowledgment
    PINGREQ(12), // PING Request
    PINGRESP(13), // PING Response
    DISCONNECT(14); // Client is disconnecting

    private final int value;

    private static final Map<Integer, MqttControlPacketType> valueToTypeMap = Arrays
            .stream(values())
            .collect(toMap(MqttControlPacketType::getValue, identity()));

    MqttControlPacketType(int value) {
        this.value = value;
    }

    public static MqttControlPacketType fromInt(int value) {
        MqttControlPacketType type = valueToTypeMap.get(value);
        if (type == null) {
            throw new IllegalArgumentException("Invalid MQTT Control Packet Type value: " + value);
        }
        return type;
    }
}
