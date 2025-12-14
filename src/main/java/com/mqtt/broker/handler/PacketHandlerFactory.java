package com.mqtt.broker.handler;

import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.packet.MqttControlPacketType;
import com.mqtt.broker.packet.MqttPacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.EnumMap;
import java.util.Map;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.*;

public class PacketHandlerFactory implements MqttPacketHandler {

    private final Map<MqttControlPacketType, MqttPacketHandler> handlers;

    public PacketHandlerFactory(BrokerContext context) {
        this.handlers = new EnumMap<>(MqttControlPacketType.class);

        handlers.put(CONNECT, new ConnectPacketHandler(context));
        handlers.put(PINGREQ, new PingReqPacketHandler());
        handlers.put(PUBLISH, new PublishPacketHandler(context));
        handlers.put(PUBACK, new PubAckPacketHandler());
        handlers.put(PUBREL, new PubRelPacketHandler(context));
        handlers.put(PUBREC, new PubRecPacketHandler());
        handlers.put(PUBCOMP, new PubCompPacketHandler());
        handlers.put(SUBSCRIBE, new SubscribePacketHandler(context));
        handlers.put(UNSUBSCRIBE, new UnsubscribePacketHandler(context));
        handlers.put(DISCONNECT, new DisconnectPacketHandler(context));
    }

    @Override
    public HandlerResult handle(SocketChannel clientChannel, MqttPacket packet) throws IOException {
        return getHandler(packet.getFixedHeader().packetType()).handle(clientChannel, packet);
    }

    private MqttPacketHandler getHandler(MqttControlPacketType packetType) {
        MqttPacketHandler handler = handlers.get(packetType);
        if (handler == null) {
            throw unsupportedPacketType(packetType);
        }
        return handler;
    }
}
