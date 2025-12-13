package com.mqtt.broker.decoder.strategy;

import com.mqtt.broker.packet.DisconnectPacket;
import com.mqtt.broker.packet.MqttFixedHeader;

import java.nio.ByteBuffer;

public class DisconnectDecoderStrategy implements DecoderStrategy<DisconnectPacket> {

    @Override
    public DisconnectPacket decode(MqttFixedHeader fixedHeader, ByteBuffer buffer) {
        return new DisconnectPacket(fixedHeader);
    }
}
