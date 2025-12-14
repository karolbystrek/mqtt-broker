package com.mqtt.broker.encoder;

import com.mqtt.broker.encoder.strategy.*;
import com.mqtt.broker.packet.*;

import java.nio.ByteBuffer;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;

public final class MqttPacketEncoder {

    private static final ConnAckEncoderStrategy CONN_ACK = new ConnAckEncoderStrategy();
    private static final PublishEncoderStrategy PUBLISH = new PublishEncoderStrategy();
    private static final PubAckEncoderStrategy PUB_ACK = new PubAckEncoderStrategy();
    private static final PubRecEncoderStrategy PUB_REC = new PubRecEncoderStrategy();
    private static final PubRelEncoderStrategy PUB_REL = new PubRelEncoderStrategy();
    private static final PubCompEncoderStrategy PUB_COMP = new PubCompEncoderStrategy();
    private static final SubAckEncoderStrategy SUB_ACK = new SubAckEncoderStrategy();
    private static final UnsubAckEncoderStrategy UNSUB_ACK = new UnsubAckEncoderStrategy();
    private static final PingRespEncoderStrategy PING_RESP = new PingRespEncoderStrategy();


    public ByteBuffer encode(MqttPacket packet) {
        return switch (packet) {
            case ConnAckPacket p -> CONN_ACK.encode(p);
            case PublishPacket p -> PUBLISH.encode(p);
            case PubAckPacket p -> PUB_ACK.encode(p);
            case PubRecPacket p -> PUB_REC.encode(p);
            case PubRelPacket p -> PUB_REL.encode(p);
            case PubCompPacket p -> PUB_COMP.encode(p);
            case SubAckPacket p -> SUB_ACK.encode(p);
            case UnsubAckPacket p -> UNSUB_ACK.encode(p);
            case PingRespPacket p -> PING_RESP.encode(p);
            default -> throw unsupportedPacketType(packet.getFixedHeader().packetType());
        };
    }
}
