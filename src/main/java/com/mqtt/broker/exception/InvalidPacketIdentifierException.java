package com.mqtt.broker.exception;

public class InvalidPacketIdentifierException extends RuntimeException {
    public InvalidPacketIdentifierException(String message) {
        super(message);
    }

    public static InvalidPacketIdentifierException invalidPacketIdentifier() {
        return new InvalidPacketIdentifierException("Packet identifier must be in range 0-65535");
    }
}
