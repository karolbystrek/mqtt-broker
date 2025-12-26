package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.ConnAckPacket;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PingRespPacket;
import com.mqtt.broker.packet.PubAckPacket;
import com.mqtt.broker.packet.PubCompPacket;
import com.mqtt.broker.packet.PubRecPacket;
import com.mqtt.broker.packet.PubRelPacket;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.packet.SubAckPacket;
import com.mqtt.broker.packet.UnsubAckPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;

public final class MqttPacketEncoder {

    private final ConnAckPacketEncoder connAck = new ConnAckPacketEncoder();
    private final PublishPacketEncoder publish = new PublishPacketEncoder();
    private final PubAckPacketEncoder pubAck = new PubAckPacketEncoder();
    private final PubRecPacketEncoder pubRec = new PubRecPacketEncoder();
    private final PubRelPacketEncoder pubRel = new PubRelPacketEncoder();
    private final PubCompPacketEncoder pubComp = new PubCompPacketEncoder();
    private final SubAckPacketEncoder subAck = new SubAckPacketEncoder();
    private final UnsubAckPacketEncoder unsubAck = new UnsubAckPacketEncoder();
    private final PingRespPacketEncoder pingResp = new PingRespPacketEncoder();

    public ByteBuffer encode(MqttPacket packet) {
        return switch (packet) {
            case ConnAckPacket p -> connAck.encode(p);
            case PublishPacket p -> publish.encode(p);
            case PubAckPacket p -> pubAck.encode(p);
            case PubRecPacket p -> pubRec.encode(p);
            case PubRelPacket p -> pubRel.encode(p);
            case PubCompPacket p -> pubComp.encode(p);
            case SubAckPacket p -> subAck.encode(p);
            case UnsubAckPacket p -> unsubAck.encode(p);
            case PingRespPacket p -> pingResp.encode(p);
            default -> throw unsupportedPacketType(packet.getFixedHeader().packetType());
        };
    }
}
