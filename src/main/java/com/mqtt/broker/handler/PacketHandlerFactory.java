package com.mqtt.broker.handler;

import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.MqttControlPacketType;
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

    public PacketHandlerFactory(BrokerContext context) {
        this.handlers = new EnumMap<>(MqttControlPacketType.class);

        handlers.put(CONNECT, new ConnectPacketHandler(context));
        handlers.put(PINGREQ, new PingReqPacketHandler());
        handlers.put(PUBLISH, new PublishPacketHandler(context));
        handlers.put(PUBACK, new PubAckPacketHandler());
        handlers.put(PUBREL, new PubRelPacketHandler());
        handlers.put(PUBREC, new PubRecPacketHandler());
        handlers.put(PUBCOMP, new PubCompPacketHandler());
        handlers.put(SUBSCRIBE, new SubscribePacketHandler(context));
        handlers.put(UNSUBSCRIBE, new UnsubscribePacketHandler(context));
        handlers.put(DISCONNECT, new DisconnectPacketHandler(context));
    }

    public MqttPacketHandler getHandler(MqttControlPacketType packetType) {
        MqttPacketHandler handler = handlers.get(packetType);
        if (handler == null) {
            throw unsupportedPacketType(packetType);
        }
        return handler;
    }
}
