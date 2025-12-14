package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.handler.strategy.*;
import com.mqtt.broker.packet.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;

public class MqttPacketHandler {

    private final ConnectPacketHandlerStrategy connect;
    private final DisconnectPacketHandlerStrategy disconnect;
    private final PingReqPacketHandlerStrategy pingReq;
    private final PubAckPacketHandlerStrategy pubAck;
    private final PubCompPacketHandlerStrategy pubComp;
    private final PubRecPacketHandlerStrategy pubRec;
    private final PubRelPacketHandlerStrategy pubRel;
    private final PublishPacketHandlerStrategy publish;
    private final SubscribePacketHandlerStrategy subscribe;
    private final UnsubscribePacketHandlerStrategy unsubscribe;

    public MqttPacketHandler(BrokerContext brokerContext) {
        this.connect = new ConnectPacketHandlerStrategy(brokerContext);
        this.disconnect = new DisconnectPacketHandlerStrategy(brokerContext);
        this.pingReq = new PingReqPacketHandlerStrategy();
        this.pubAck = new PubAckPacketHandlerStrategy();
        this.pubComp = new PubCompPacketHandlerStrategy();
        this.pubRec = new PubRecPacketHandlerStrategy();
        this.pubRel = new PubRelPacketHandlerStrategy(brokerContext);
        this.publish = new PublishPacketHandlerStrategy(brokerContext);
        this.subscribe = new SubscribePacketHandlerStrategy(brokerContext);
        this.unsubscribe = new UnsubscribePacketHandlerStrategy(brokerContext);
    }

    public HandlerResult handle(SocketChannel channel, MqttPacket packet) throws IOException {
        return switch (packet) {
            case ConnectPacket p -> connect.handle(channel, p);
            case DisconnectPacket p -> disconnect.handle(channel, p);
            case PingReqPacket p -> pingReq.handle(channel, p);
            case PubAckPacket p -> pubAck.handle(channel, p);
            case PubCompPacket p -> pubComp.handle(channel, p);
            case PubRecPacket p -> pubRec.handle(channel, p);
            case PubRelPacket p -> pubRel.handle(channel, p);
            case PublishPacket p -> publish.handle(channel, p);
            case SubscribePacket p -> subscribe.handle(channel, p);
            case UnsubscribePacket p -> unsubscribe.handle(channel, p);
            default -> throw unsupportedPacketType(packet.getFixedHeader().packetType());
        };
    }
}
