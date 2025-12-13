package com.mqtt.broker.persistence;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mqtt.broker.Session;
import com.mqtt.broker.packet.PublishPacket;

public class MqttPersistenceModule extends SimpleModule {

    public MqttPersistenceModule() {
        setMixInAnnotation(Session.class, SessionMixin.class);
        addSerializer(PublishPacket.class, new PublishPacketSerializer());
        addDeserializer(PublishPacket.class, new PublishPacketDeserializer());
    }
}
