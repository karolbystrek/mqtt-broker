package com.mqtt.broker.handler;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PingReqPacket;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubCompPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PubRelPacket;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.packet.SubscribePacket;
import com.mqtt.broker.packet.UnsubscribePacket;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;

public class MqttPacketHandler {

    private final ConnectPacketHandler connect;
    private final DisconnectPacketHandler disconnect;
    private final PingReqPacketHandler pingReq;
    private final PubAckPacketHandler pubAck;
    private final PubCompPacketHandler pubComp;
    private final PubRecPacketHandler pubRec;
    private final PubRelPacketHandler pubRel;
    private final PublishPacketHandler publish;
    private final SubscribePacketHandler subscribe;
    private final UnsubscribePacketHandler unsubscribe;

    public MqttPacketHandler(BrokerContext brokerContext) {
        this.connect = new ConnectPacketHandler(brokerContext);
        this.disconnect = new DisconnectPacketHandler(brokerContext);
        this.pingReq = new PingReqPacketHandler();
        this.pubAck = new PubAckPacketHandler();
        this.pubComp = new PubCompPacketHandler();
        this.pubRec = new PubRecPacketHandler();
        this.pubRel = new PubRelPacketHandler(brokerContext);
        this.publish = new PublishPacketHandler(brokerContext);
        this.subscribe = new SubscribePacketHandler(brokerContext);
        this.unsubscribe = new UnsubscribePacketHandler(brokerContext);
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
            default -> throw unsupportedPacketType(packet.fixedHeader().packetType());
        };
    }
}
