package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import com.mqtt.broker.MqttQoS;
import lombok.Getter;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.PUBLISH;

@Getter
public final class PublishPacket extends MqttPacket {

    private final PublishVariableHeader variableHeader;
    private final byte[] payload; // payload is raw bytes as it can be anything


    PublishPacket(MqttFixedHeader fixedHeader, PublishVariableHeader variableHeader, byte[] payload) {
        super(fixedHeader);
        if (fixedHeader.packetType() != PUBLISH) {
            throw invalidPacketType(PublishPacket.class);
        }
        this.variableHeader = variableHeader;
        this.payload = payload;
    }

    public record PublishVariableHeader(
            String topicName,
            int packetIdentifier // Optional, only present for QoS levels 1 and 2
    ) {
    }

    public boolean isDup() {
        return (getFixedHeader().flags() & 0b0000_1000) != 0;
    }

    public MqttQoS getQosLevel() {
        int qosValue = (getFixedHeader().flags() & 0b0000_0110) >> 1;
        return MqttQoS.fromInt(qosValue);
    }

    public boolean isRetain() {
        return (getFixedHeader().flags() & 0b0000_0001) != 0;
    }
}
