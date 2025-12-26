package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.decoder.DecoderUtils.decodeFixedHeader;
import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;

public class MqttPacketDecoder {

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
        if (buffer.remaining() < 2) {
            return null; // Not enough data to read fixed header + one byte of remaining length
        }

        buffer.mark(); // mark the current position in case of incomplete packet

        var fixedHeader = decodeFixedHeader(buffer);
        if (fixedHeader == null) {
            buffer.reset(); // we have the full fixed header but not the full packet
            return null;
        }

        ByteBuffer packetBody = buffer.slice();
        packetBody.limit(fixedHeader.remainingLength());

        buffer.position(buffer.position() + fixedHeader.remainingLength());

        return switch (fixedHeader.packetType()) {
            case CONNECT -> connect.decode(fixedHeader, packetBody);
            case DISCONNECT -> disconnect.decode(fixedHeader, packetBody);
            case PUBLISH -> publish.decode(fixedHeader, packetBody);
            case PUBACK -> pubAck.decode(fixedHeader, packetBody);
            case PINGREQ -> pingReq.decode(fixedHeader, packetBody);
            case PUBREC -> pubRec.decode(fixedHeader, packetBody);
            case PUBREL -> pubRel.decode(fixedHeader, packetBody);
            case PUBCOMP -> pubComp.decode(fixedHeader, packetBody);
            case SUBSCRIBE -> subscribe.decode(fixedHeader, packetBody);
            case UNSUBSCRIBE -> unsubscribe.decode(fixedHeader, packetBody);
            default -> throw unsupportedPacketType(fixedHeader.packetType());
        };
    }
}
