package com.mqtt.broker.decoder;

import java.nio.ByteBuffer;

public interface MqttPacketDecoderInterface {

    String decodeString(ByteBuffer buffer);

    int decodeTwoByteInt(ByteBuffer buffer);
}
