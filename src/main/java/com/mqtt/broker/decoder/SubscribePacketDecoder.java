package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttQoS;
import com.mqtt.broker.packet.SubscribePacket;

import java.util.ArrayList;

import static com.mqtt.broker.decoder.DecoderUtils.decodeString;
import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;
import static com.mqtt.broker.packet.SubscribePacket.Subscription;

class SubscribePacketDecoder implements PacketDecoder<SubscribePacket> {

    private static final int QOS_MASK = 0x03;

    @Override
    public SubscribePacket decode(MqttFrame frame) {
        var fixedHeader = frame.fixedHeader();
        var body = frame.body();

        int packetIdentifier = decodeTwoByteInt(body);
        var subscriptions = new ArrayList<Subscription>();

        while (body.hasRemaining()) {
            String topicFilter = decodeString(body);
            int requestedQos = body.get() & QOS_MASK;
            subscriptions.add(new Subscription(topicFilter, MqttQoS.fromInt(requestedQos)));
        }

        return new SubscribePacket(fixedHeader, packetIdentifier, subscriptions);
    }
}
