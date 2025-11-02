package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.MqttControlPacketType;

import java.nio.channels.SocketChannel;
import java.util.EnumMap;
import java.util.Map;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.CONNECT;
import static com.mqtt.broker.packet.MqttControlPacketType.DISCONNECT;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGREQ;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREL;
import static com.mqtt.broker.packet.MqttControlPacketType.SUBSCRIBE;

public class PacketHandlerFactory {

    private final Map<MqttControlPacketType, MqttPacketHandler> handlers;

    public PacketHandlerFactory(Map<SocketChannel, Session> activeSessions, Map<String, Session> persistentSessions) {
        this.handlers = new EnumMap<>(MqttControlPacketType.class);

        handlers.put(CONNECT, new ConnectPacketHandler(activeSessions, persistentSessions));
        handlers.put(PINGREQ, new PingReqPacketHandler());
        handlers.put(PUBLISH, new PublishPacketHandler());
        handlers.put(PUBREL, new PubRelPacketHandler());
        handlers.put(SUBSCRIBE, new SubscribePacketHandler(activeSessions));
        handlers.put(DISCONNECT, new DisconnectPacketHandler(activeSessions, persistentSessions));
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
