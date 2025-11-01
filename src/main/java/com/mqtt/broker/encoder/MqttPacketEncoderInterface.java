package com.mqtt.broker.encoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttPacket;

import java.nio.ByteBuffer;

public interface MqttPacketEncoderInterface {

    ByteBuffer encode(MqttPacket packet);

    ByteBuffer encodeFixedHeader(MqttFixedHeader header);

    void encodeString(ByteBuffer buffer, String s);
}
