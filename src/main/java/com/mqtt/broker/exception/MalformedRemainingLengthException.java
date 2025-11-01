package com.mqtt.broker.exception;

public class MalformedRemainingLengthException extends RuntimeException {
    public MalformedRemainingLengthException(String message) {
        super(message);
    }

    public static MalformedRemainingLengthException malformedRemainingLength() {
        return new MalformedRemainingLengthException("Malformed Remaining Length while decoding MQTT packet");
    }
}
