package com.mqtt.broker.handler;

import com.mqtt.broker.packet.MqttControlPacketType;
import com.mqtt.broker.encoder.MqttPacketEncoder;

import java.util.EnumMap;
import java.util.Map;

import static com.mqtt.broker.packet.MqttControlPacketType.CONNECT;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;
import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;

public class PacketHandlerFactory {

    private final Map<MqttControlPacketType, MqttPacketHandler> handlers = new EnumMap<>(MqttControlPacketType.class);

    public PacketHandlerFactory() {
        MqttPacketEncoder encoder = new MqttPacketEncoder();

        handlers.put(CONNECT, new ConnectPacketHandler(encoder));
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
