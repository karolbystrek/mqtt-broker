package com.mqtt.broker.packet;

public sealed interface MqttPacket permits
        ConnAckPacket,
        ConnectPacket,
        DisconnectPacket,
        PingReqPacket,
        PingRespPacket,
        PubAckPacket,
        PubCompPacket,
        PublishPacket,
        PubRecPacket,
        PubRelPacket,
        SubAckPacket,
        SubscribePacket,
        UnsubAckPacket,
        UnsubscribePacket {

    MqttFixedHeader fixedHeader();
}