package com.mqtt.broker.packet;

public record MqttFixedHeader(
        MqttControlPacketType packetType,
        byte flags,
        int remainingLength
) {
}
