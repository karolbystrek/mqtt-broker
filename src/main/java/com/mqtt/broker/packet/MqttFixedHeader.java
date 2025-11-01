package com.mqtt.broker.packet;

/**
 * Represents the Fixed Header present in all MQTT Control Packets.
 *
 * @param packetType      The type of the MQTT packet.
 * @param flags           The 4 bits of flags specific to the packet type.
 * @param remainingLength The length of the variable header plus the payload.
 */
public record MqttFixedHeader(
        MqttControlPacketType packetType,
        byte flags,
        int remainingLength
) {
}
