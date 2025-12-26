package com.mqtt.broker.packet;

public record MqttFixedHeader(
        MqttPacketType packetType,
        byte flags,
        int remainingLength
) {
}
