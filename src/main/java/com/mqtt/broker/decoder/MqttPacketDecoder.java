package com.mqtt.broker.decoder;

import com.mqtt.broker.decoder.strategy.*;
import com.mqtt.broker.packet.MqttControlPacketType;
import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.mqtt.broker.decoder.DecoderUtils.decodeFixedHeader;
import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.*;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public class MqttPacketDecoder {

    private final Map<MqttControlPacketType, DecoderStrategy<?>> decoders;

    public MqttPacketDecoder() {
        this.decoders = ofEntries(
                entry(CONNECT, new ConnectDecoderStrategy()),
                entry(DISCONNECT, new DisconnectDecoderStrategy()),
                entry(PUBLISH, new PublishDecoderStrategy()),
                entry(PUBACK, new PubAckDecoderStrategy()),
                entry(PINGREQ, new PingReqDecoderStrategy()),
                entry(PUBREC, new PubRecDecoderStrategy()),
                entry(PUBREL, new PubRelDecoderStrategy()),
                entry(PUBCOMP, new PubCompDecoderStrategy()),
                entry(SUBSCRIBE, new SubscribeDecoderStrategy()),
                entry(UNSUBSCRIBE, new UnsubscribeDecoderStrategy())
        );
    }

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

        return getDecoderFor(fixedHeader.packetType()).decode(fixedHeader, packetBody);
    }

    private DecoderStrategy<?> getDecoderFor(MqttControlPacketType packetType) {
        var decoder = decoders.get(packetType);
        if (decoder == null) {
            throw unsupportedPacketType(packetType);
        }
        return decoder;
    }
}
