package com.mqtt.broker.decoder.strategy;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PingReqPacket;

import java.nio.ByteBuffer;

public class PingReqDecoderStrategy implements DecoderStrategy<PingReqPacket> {

    @Override
    public PingReqPacket decode(MqttFixedHeader fixedHeader, ByteBuffer buffer) {
        return new PingReqPacket(fixedHeader);
    }
}
