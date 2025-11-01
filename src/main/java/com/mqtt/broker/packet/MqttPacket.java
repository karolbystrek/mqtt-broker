package com.mqtt.broker.packet;

import lombok.Getter;

@Getter
public abstract class MqttPacket {

    private final MqttFixedHeader fixedHeader;

    protected MqttPacket(MqttFixedHeader fixedHeader) {
        this.fixedHeader = fixedHeader;
    }
}
