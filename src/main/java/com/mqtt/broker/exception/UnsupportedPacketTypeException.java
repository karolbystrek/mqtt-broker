package com.mqtt.broker.exception;

import com.mqtt.broker.packet.MqttPacketType;

public class UnsupportedPacketTypeException extends RuntimeException {
    public UnsupportedPacketTypeException(String message) {
        super(message);
    }

    public static UnsupportedPacketTypeException unsupportedPacketType(MqttPacketType packetType) {
        return new UnsupportedPacketTypeException("Unsupported packet type: " + packetType);
    }
}
