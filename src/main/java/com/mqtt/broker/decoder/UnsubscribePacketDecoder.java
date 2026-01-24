package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.UnsubscribePacket;

import java.util.ArrayList;

import static com.mqtt.broker.decoder.DecoderUtils.decodeString;
import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;

class UnsubscribePacketDecoder implements PacketDecoder<UnsubscribePacket> {

    @Override
    public UnsubscribePacket decode(MqttFrame frame) {
        var fixedHeader = frame.fixedHeader();
        var body = frame.body();

        int packetIdentifier = decodeTwoByteInt(body);
        var topicFilters = new ArrayList<String>();

        while (body.hasRemaining()) {
            topicFilters.add(decodeString(body));
        }

        return new UnsubscribePacket(fixedHeader, packetIdentifier, topicFilters);
    }
}
