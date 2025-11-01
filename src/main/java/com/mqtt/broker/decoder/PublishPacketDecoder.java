package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PublishPacket;

import java.nio.ByteBuffer;

import static com.mqtt.broker.packet.PublishPacket.PublishVariableHeader;

public interface PublishPacketDecoder extends MqttPacketDecoderInterface {

    default PublishPacket decodePublish(MqttFixedHeader fixedHeader, ByteBuffer body) {
        String topicName = decodeString(body);
        int packetIdentifier = 0;

        if (((fixedHeader.flags() >> 1) & 0x03) > 0) {
            packetIdentifier = decodeTwoByteInt(body); // packet identifier is present if QoS > 0
        }

        var variableHeader = new PublishVariableHeader(topicName, packetIdentifier);

        byte[] payload = new byte[body.remaining()];
        body.get(payload);

        return new PublishPacket(fixedHeader, variableHeader, payload);
    }
}
