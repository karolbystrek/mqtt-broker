package com.mqtt.broker;

public class InvalidPacketType extends RuntimeException {
    public InvalidPacketType(String message) {
        super(message);
    }

    public static InvalidPacketType invalidPacketType(Class<?> packetClass) {
        return new InvalidPacketType("Invalid packet type for " + packetClass.getSimpleName());
    }
}
