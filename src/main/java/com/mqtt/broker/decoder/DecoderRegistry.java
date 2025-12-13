package com.mqtt.broker.decoder;

import com.mqtt.broker.decoder.strategy.*;
import com.mqtt.broker.packet.MqttControlPacketType;

import java.util.Map;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.*;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

final class DecoderRegistry {

    private final Map<MqttControlPacketType, DecoderStrategy<?>> decoders;

    DecoderRegistry() {
        this.decoders = ofEntries(
                entry(CONNECT, new ConnectDecoderStrategy()),
                entry(DISCONNECT, new DisconnectDecoderStrategy()),
                entry(PUBLISH, new PublishDecoderStrategy()),
                entry(PUBACK, new PubAckDecoderStrategy()),
                entry(PINGREQ, new PingReqDecoderStrategy()),
                entry(PUBREC, new PubRecDecoderStrategy()),
                entry(PUBREL, new PubRelDecoderStrategy()),
                entry(PUBCOMP, new PubCompDecoderStrategy()),
                entry(SUBSCRIBE, new SubscribeDecoderStrategy()),
                entry(UNSUBSCRIBE, new UnsubscribeDecoderStrategy())
        );
    }

    DecoderStrategy<?> getDecoderFor(MqttControlPacketType packetType) {
        var decoder = decoders.get(packetType);
        if (decoder == null) {
            throw unsupportedPacketType(packetType);
        }
        return decoder;
    }
}
