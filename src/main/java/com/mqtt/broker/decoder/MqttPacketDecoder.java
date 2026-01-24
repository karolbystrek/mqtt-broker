package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;

public class MqttPacketDecoder implements ProtocolDecoder {

    private final ConnectPacketDecoder connect = new ConnectPacketDecoder();
    private final DisconnectPacketDecoder disconnect = new DisconnectPacketDecoder();
    private final PublishPacketDecoder publish = new PublishPacketDecoder();
    private final PubAckPacketDecoder pubAck = new PubAckPacketDecoder();
    private final PingReqPacketDecoder pingReq = new PingReqPacketDecoder();
    private final PubRecPacketDecoder pubRec = new PubRecPacketDecoder();
    private final PubRelPacketDecoder pubRel = new PubRelPacketDecoder();
    private final PubCompPacketDecoder pubComp = new PubCompPacketDecoder();
    private final SubscribePacketDecoder subscribe = new SubscribePacketDecoder();
    private final UnsubscribePacketDecoder unsubscribe = new UnsubscribePacketDecoder();

    public MqttPacket decode(ByteBuffer buffer) {
        return MqttFrame.read(buffer)
                .map(this::decodeFrame)
                .orElse(null);
    }

    private MqttPacket decodeFrame(MqttFrame frame) {
        return switch (frame.fixedHeader().packetType()) {
            case CONNECT -> connect.decode(frame);
            case DISCONNECT -> disconnect.decode(frame);
            case PUBLISH -> publish.decode(frame);
            case PUBACK -> pubAck.decode(frame);
            case PINGREQ -> pingReq.decode(frame);
            case PUBREC -> pubRec.decode(frame);
            case PUBREL -> pubRel.decode(frame);
            case PUBCOMP -> pubComp.decode(frame);
            case SUBSCRIBE -> subscribe.decode(frame);
            case UNSUBSCRIBE -> unsubscribe.decode(frame);
            default -> throw unsupportedPacketType(frame.fixedHeader().packetType());
        };
    }
}
