package com.mqtt.broker.packets;

import com.mqtt.broker.MqttFixedHeader;
import lombok.Getter;

import java.util.List;

import static com.mqtt.broker.InvalidPacketType.invalidPacketType;
import static com.mqtt.broker.MqttControlPacketType.SUBACK;
import static java.util.List.copyOf;

@Getter
public final class SubAckPacket extends MqttPacket {

    private final int packetIdentifier;
    private final List<Integer> grantedQosLevels;

    public SubAckPacket(MqttFixedHeader fixedHeader, int packetIdentifier, List<Integer> grantedQosLevels) {
        super(fixedHeader);
        if (fixedHeader.packetType() != SUBACK) {
            throw invalidPacketType(SubAckPacket.class);
        }
        this.packetIdentifier = packetIdentifier;
        this.grantedQosLevels = copyOf(grantedQosLevels);
    }
}
