package com.mqtt.broker.decoder;

import com.mqtt.broker.MqttFixedHeader;
import com.mqtt.broker.packet.UnsubscribePacket;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public interface UnsubscribePacketDecoder extends MqttPacketDecoderInterface {

    default UnsubscribePacket decodeUnsubscribe(MqttFixedHeader fixedHeader, ByteBuffer body) {
        int packetIdentifier = decodeTwoByteInt(body);
        var topicFilters = new ArrayList<String>();

        while (body.hasRemaining()) {
            topicFilters.add(decodeString(body));
        }

        return new UnsubscribePacket(fixedHeader, packetIdentifier, topicFilters);
    }
}
