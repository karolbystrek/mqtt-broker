package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PingRespPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.encoder.EncoderUtils.encodeFixedHeader;

class PingRespPacketEncoder implements PacketEncoder<PingRespPacket> {

    @Override
    public ByteBuffer encode(PingRespPacket packet) {
        return encodeFixedHeader(packet.fixedHeader());
    }
}
