package com.mqtt.broker.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.packet.MqttControlPacketType;
import com.mqtt.broker.service.PendingMessageDeliveryService;
import com.mqtt.broker.trie.TopicTree;

import java.nio.channels.SocketChannel;
import java.util.EnumMap;
import java.util.Map;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.CONNECT;
import static com.mqtt.broker.packet.MqttControlPacketType.DISCONNECT;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGREQ;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBCOMP;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREC;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREL;
import static com.mqtt.broker.packet.MqttControlPacketType.SUBSCRIBE;
import static com.mqtt.broker.packet.MqttControlPacketType.UNSUBSCRIBE;

public class PacketHandlerFactory {

    private final Map<MqttControlPacketType, MqttPacketHandler> handlers;

    public PacketHandlerFactory(Map<SocketChannel, Session> activeSessions,
                                Map<String, Session> persistentSessions,
                                TopicTree topicTree,
                                Map<String, SocketChannel> clientIdToChannel,
                                PendingMessageDeliveryService pendingMessageService) {
        this.handlers = new EnumMap<>(MqttControlPacketType.class);

        handlers.put(CONNECT, new ConnectPacketHandler(activeSessions, persistentSessions, clientIdToChannel, topicTree, pendingMessageService));
        handlers.put(PINGREQ, new PingReqPacketHandler());
        handlers.put(PUBLISH, new PublishPacketHandler(topicTree, clientIdToChannel, persistentSessions));
        handlers.put(PUBACK, new PubAckPacketHandler());
        handlers.put(PUBREL, new PubRelPacketHandler());
        handlers.put(PUBREC, new PubRecPacketHandler());
        handlers.put(PUBCOMP, new PubCompPacketHandler());
        handlers.put(SUBSCRIBE, new SubscribePacketHandler(activeSessions, topicTree));
        handlers.put(UNSUBSCRIBE, new UnsubscribePacketHandler(activeSessions, topicTree));
        handlers.put(DISCONNECT, new DisconnectPacketHandler(activeSessions, persistentSessions, topicTree, clientIdToChannel));
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
