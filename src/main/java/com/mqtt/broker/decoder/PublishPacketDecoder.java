package com.mqtt.broker.decoder;

import com.mqtt.broker.packet.PublishPacket;

import static com.mqtt.broker.decoder.DecoderUtils.decodeString;
import static com.mqtt.broker.decoder.DecoderUtils.decodeTwoByteInt;
import static com.mqtt.broker.packet.PublishPacket.PublishVariableHeader;

class PublishPacketDecoder implements PacketDecoder<PublishPacket> {

    private static final int QOS_SHIFT = 1;
    private static final int QOS_MASK = 0x03;

    @Override
    public PublishPacket decode(MqttFrame frame) {
        var fixedHeader = frame.fixedHeader();
        var body = frame.body();

        String topicName = decodeString(body);
        int packetIdentifier = 0;

        if (((fixedHeader.flags() >> QOS_SHIFT) & QOS_MASK) > 0) {
            packetIdentifier = decodeTwoByteInt(body); // packet identifier is present if QoS > 0
        }

        var variableHeader = new PublishVariableHeader(topicName, packetIdentifier);

        byte[] payload = new byte[body.remaining()];
        body.get(payload);

        return new PublishPacket(fixedHeader, variableHeader, payload);
    }
}
