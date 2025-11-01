package com.mqtt.broker.decoder;

import com.mqtt.broker.MqttFixedHeader;
import com.mqtt.broker.MqttQoS;
import com.mqtt.broker.packet.SubscribePacket;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static com.mqtt.broker.packet.SubscribePacket.Subscription;

public interface SubscribePacketDecoder extends MqttPacketDecoderInterface {

    default SubscribePacket decodeSubscribe(MqttFixedHeader fixedHeader, ByteBuffer body) {
        int packetIdentifier = decodeTwoByteInt(body);
        var subscriptions = new ArrayList<Subscription>();

        while (body.hasRemaining()) {
            String topicFilter = decodeString(body);
            int requestedQos = body.get() & 0x03;
            subscriptions.add(new Subscription(topicFilter, MqttQoS.fromInt(requestedQos)));
        }

        return new SubscribePacket(fixedHeader, packetIdentifier, subscriptions);
    }
}
