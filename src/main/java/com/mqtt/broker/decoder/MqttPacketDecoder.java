package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.MqttPacketType;

import java.nio.ByteBuffer;
import java.util.Map;

import static com.mqtt.broker.decoder.DecoderUtils.decodeFixedHeader;
import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static com.mqtt.broker.packet.MqttPacketType.CONNECT;
import static com.mqtt.broker.packet.MqttPacketType.DISCONNECT;
import static com.mqtt.broker.packet.MqttPacketType.PINGREQ;
import static com.mqtt.broker.packet.MqttPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttPacketType.PUBCOMP;
import static com.mqtt.broker.packet.MqttPacketType.PUBLISH;
import static com.mqtt.broker.packet.MqttPacketType.PUBREC;
import static com.mqtt.broker.packet.MqttPacketType.PUBREL;
import static com.mqtt.broker.packet.MqttPacketType.SUBSCRIBE;
import static com.mqtt.broker.packet.MqttPacketType.UNSUBSCRIBE;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

public class MqttPacketDecoder {

    private final Map<MqttPacketType, PacketDecoder<?>> decoders;

    public MqttPacketDecoder() {
        this.decoders = ofEntries(
                entry(CONNECT, new ConnectPacketDecoder()),
                entry(DISCONNECT, new DisconnectPacketDecoder()),
                entry(PUBLISH, new PublishPacketDecoder()),
                entry(PUBACK, new PubAckPacketDecoder()),
                entry(PINGREQ, new PingReqPacketDecoder()),
                entry(PUBREC, new PubRecPacketDecoder()),
                entry(PUBREL, new PubRelPacketDecoder()),
                entry(PUBCOMP, new PubCompPacketDecoder()),
                entry(SUBSCRIBE, new SubscribePacketDecoder()),
                entry(UNSUBSCRIBE, new UnsubscribePacketDecoder())
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

    private PacketDecoder<?> getDecoderFor(MqttPacketType packetType) {
        var decoder = decoders.get(packetType);
        if (decoder == null) {
            throw unsupportedPacketType(packetType);
        }
        return decoder;
    }
}
