package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.packet.PingReqPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.decoder.PacketDecoderUtils.decodeFixedHeader;
import static com.mqtt.broker.packet.MqttControlPacketType.DISCONNECT;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGREQ;

public class MqttPacketDecoder {

    private final DecoderRegistry decoderRegistry = new DecoderRegistry();

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

        if (fixedHeader.packetType() == PINGREQ) {
            return new PingReqPacket(fixedHeader);
        } else if (fixedHeader.packetType() == DISCONNECT) {
            return new DisconnectPacket(fixedHeader);
        }

        return decoderRegistry.getDecoderFor(fixedHeader.packetType()).decode(fixedHeader, packetBody);
    }
}
