package com.mqtt.broker.persistence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mqtt.broker.packet.PublishPacket;

import java.io.IOException;

class PublishPacketSerializer extends JsonSerializer<PublishPacket> {
    @Override
    public void serialize(PublishPacket value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("topic", value.getVariableHeader().topicName());
        gen.writeNumberField("packetId", value.getVariableHeader().packetIdentifier());
        gen.writeNumberField("qos", value.getQosLevel().getValue());
        gen.writeBooleanField("retain", value.isRetain());
        gen.writeBooleanField("dup", value.isDup());
        gen.writeBinaryField("payload", value.getPayload());
        gen.writeEndObject();
    }
}
