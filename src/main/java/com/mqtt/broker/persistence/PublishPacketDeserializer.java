package com.mqtt.broker.persistence;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.PublishPacket;

import java.io.IOException;

import static com.mqtt.broker.packet.MqttPacketType.PUBLISH;

class PublishPacketDeserializer extends JsonDeserializer<PublishPacket> {
    @Override
    public PublishPacket deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        String topic = node.get("topic").asText();
        int packetId = node.has("packetId") ? node.get("packetId").asInt() : 0;
        int qosVal = node.get("qos").asInt();
        boolean retain = node.get("retain").asBoolean();
        boolean dup = node.get("dup").asBoolean();
        byte[] payload = node.has("payload") ? node.get("payload").binaryValue() : new byte[0];

        byte flags = 0;
        if (dup) flags |= 0x08;
        if (retain) flags |= 0x01;
        flags |= (byte) (qosVal << 1);

        int remLength = topic.getBytes().length + 2 + payload.length;
        if (qosVal > 0) remLength += 2;

        var fixedHeader = new MqttFixedHeader(PUBLISH, flags, remLength);
        var variableHeader = new PublishPacket.PublishVariableHeader(topic, packetId);

        return new PublishPacket(fixedHeader, variableHeader, payload);
    }
}
