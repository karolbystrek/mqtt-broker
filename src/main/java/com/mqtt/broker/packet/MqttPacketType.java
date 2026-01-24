package com.mqtt.broker.packet;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Getter
public enum MqttPacketType {
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
    DISCONNECT(14), // Client is disconnecting
    UNKNOWN(0); // Unsupported or unknown packet type

    private final int value;

    private static final int PACKET_TYPE_MASK = 0x0F;
    private static final int PACKET_TYPE_SHIFT = 4;

    private static final Map<Integer, MqttPacketType> valueToTypeMap = Arrays
            .stream(values())
            .collect(toMap(MqttPacketType::getValue, identity()));

    MqttPacketType(int value) {
        this.value = value;
    }

    public static MqttPacketType fromHeaderByte(byte headerByte) {
        int value = (headerByte >> PACKET_TYPE_SHIFT) & PACKET_TYPE_MASK;
        return fromInt(value);
    }

    public static MqttPacketType fromInt(int value) {
        return valueToTypeMap.getOrDefault(value, UNKNOWN);
    }
}
