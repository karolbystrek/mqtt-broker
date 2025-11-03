package com.mqtt.broker.encoder;

import com.mqtt.broker.encoder.strategy.ConnAckEncoderStrategy;
import com.mqtt.broker.encoder.strategy.PacketEncoderStrategy;
import com.mqtt.broker.encoder.strategy.PingRespEncoderStrategy;
import com.mqtt.broker.encoder.strategy.PubAckEncoderStrategy;
import com.mqtt.broker.encoder.strategy.PubCompEncoderStrategy;
import com.mqtt.broker.encoder.strategy.PubRecEncoderStrategy;
import com.mqtt.broker.encoder.strategy.PubRelEncoderStrategy;
import com.mqtt.broker.encoder.strategy.PublishEncoderStrategy;
import com.mqtt.broker.encoder.strategy.SubAckEncoderStrategy;
import com.mqtt.broker.encoder.strategy.UnsubAckEncoderStrategy;
import com.mqtt.broker.packet.MqttControlPacketType;

import java.util.Map;

import static com.mqtt.broker.exception.UnsupportedPacketTypeException.unsupportedPacketType;
import static com.mqtt.broker.packet.MqttControlPacketType.CONNACK;
import static com.mqtt.broker.packet.MqttControlPacketType.PINGRESP;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBACK;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBCOMP;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREC;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBREL;
import static com.mqtt.broker.packet.MqttControlPacketType.SUBACK;
import static com.mqtt.broker.packet.MqttControlPacketType.UNSUBACK;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

final class EncoderRegistry {

    private final Map<MqttControlPacketType, PacketEncoderStrategy> encoders;

    EncoderRegistry() {
        this.encoders = ofEntries(
                entry(CONNACK, new ConnAckEncoderStrategy()),
                entry(PINGRESP, new PingRespEncoderStrategy()),
                entry(PUBLISH, new PublishEncoderStrategy()),
                entry(PUBACK, new PubAckEncoderStrategy()),
                entry(PUBREC, new PubRecEncoderStrategy()),
                entry(PUBREL, new PubRelEncoderStrategy()),
                entry(PUBCOMP, new PubCompEncoderStrategy()),
                entry(SUBACK, new SubAckEncoderStrategy()),
                entry(UNSUBACK, new UnsubAckEncoderStrategy())
        );
    }

    PacketEncoderStrategy getEncoder(MqttControlPacketType packetType) {
        var encoder = encoders.get(packetType);
        if (encoder == null) {
            throw unsupportedPacketType(packetType);
        }
        return encoder;
    }
}
