package com.mqtt.broker.pipeline;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.handler.MqttPacketHandler;
import com.mqtt.broker.pipeline.handler.*;

import java.util.List;

public class PipelineFactory {

    public static Pipeline create(BrokerContext context) {
        return new Pipeline(List.of(
                new LoggingHandler(),
                new ClientActivityHandler(),
                new PacketProcessingHandler(new MqttPacketHandler(context)),
                new ResponseDeliveryHandler(),
                new EventPublishingHandler(context.getEventPublisher())
        ));
    }
}
