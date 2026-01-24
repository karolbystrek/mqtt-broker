package com.mqtt.broker.session.persistence;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mqtt.broker.packet.PublishPacket;
import com.mqtt.broker.session.Session;

public class MqttPersistenceModule extends SimpleModule {

    public MqttPersistenceModule() {
        setMixInAnnotation(Session.class, SessionMixin.class);
        addSerializer(PublishPacket.class, new PublishPacketSerializer());
        addDeserializer(PublishPacket.class, new PublishPacketDeserializer());
    }
}
