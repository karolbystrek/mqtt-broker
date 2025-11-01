package com.mqtt.broker.exception;

public class InvalidPacketTypeException extends RuntimeException {
    public InvalidPacketTypeException(String message) {
        super(message);
    }

    public static InvalidPacketTypeException invalidPacketType(Class<?> packetClass) {
        return new InvalidPacketTypeException("Invalid packet type for " + packetClass.getSimpleName());
    }
}
