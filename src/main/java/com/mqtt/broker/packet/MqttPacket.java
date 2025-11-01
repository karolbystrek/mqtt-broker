package com.mqtt.broker.packet;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

@Getter
public abstract class MqttPacket {

    private final MqttFixedHeader fixedHeader;

    protected MqttPacket(MqttFixedHeader fixedHeader) {
        this.fixedHeader = fixedHeader;
    }
}
