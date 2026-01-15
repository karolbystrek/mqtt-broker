package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttPacket;
import java.nio.ByteBuffer;

public interface ProtocolDecoder {
    MqttPacket decode(ByteBuffer buffer);
}
