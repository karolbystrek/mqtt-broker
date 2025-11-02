package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.PingRespPacket;

import java.nio.ByteBuffer;

public interface PingRespPacketEncoder extends MqttPacketEncoderInterface {

    default ByteBuffer encodePingResp(PingRespPacket packet) {
        return encodeFixedHeader(packet.getFixedHeader());
    }
}
