package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.packet.MqttControlPacketType;

import java.util.EnumMap;
import java.util.Map;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.CONNECT;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;

public class PacketHandlerFactory {

    private final Map<MqttControlPacketType, MqttPacketHandler> handlers;

    public PacketHandlerFactory(MqttPacketEncoder encoder, Map<String, Session> sessions) {
        this.handlers = new EnumMap<>(MqttControlPacketType.class);

        handlers.put(CONNECT, new ConnectPacketHandler(encoder, sessions));
        handlers.put(PUBLISH, new PublishPacketHandler(encoder));
        // TODO: Register all supported packet handlers
    }

    public MqttPacketHandler getHandler(MqttControlPacketType packetType) {
        MqttPacketHandler handler = handlers.get(packetType);
        if (handler == null) {
            throw unsupportedPacketType(packetType);
        }
        return handler;
    }
}
