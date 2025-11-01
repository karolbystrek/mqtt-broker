package com.mqtt.broker.exception;

import com.mqtt.broker.MqttControlPacketType;

public class UnsupportedPacketTypeException extends RuntimeException {
    public UnsupportedPacketTypeException(String message) {
        super(message);
    }

    public static UnsupportedPacketTypeException unsupportedPacketType(MqttControlPacketType packetType) {
        return new UnsupportedPacketTypeException("Unsupported packet type: " + packetType);
    }
}
