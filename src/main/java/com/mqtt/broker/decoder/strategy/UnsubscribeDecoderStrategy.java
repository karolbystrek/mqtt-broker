package com.mqtt.broker.decoder.strategy;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.UnsubscribePacket;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static com.mqtt.broker.decoder.DecoderUtils.decodeString;
import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

public class UnsubscribeDecoderStrategy implements DecoderStrategy<UnsubscribePacket> {

    @Override
    public UnsubscribePacket decode(MqttFixedHeader fixedHeader, ByteBuffer body) {
        int packetIdentifier = decodeTwoByteInt(body);
        var topicFilters = new ArrayList<String>();

        while (body.hasRemaining()) {
            topicFilters.add(decodeString(body));
        }

        return new UnsubscribePacket(fixedHeader, packetIdentifier, topicFilters);
    }
}
