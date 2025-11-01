package com.mqtt.broker.exception;

public class UnsupportedProtocolVersionException extends RuntimeException {
    public UnsupportedProtocolVersionException(String message) {
        super(message);
    }

    public static UnsupportedProtocolVersionException unsupportedProtocolVersion(int version) {
        return new UnsupportedProtocolVersionException("Unsupported protocol version: " + version);
    }
}
