package com.mqtt.broker.pipeline.handler;

import com.mqtt.broker.pipeline.PipelineContext;
import com.mqtt.broker.pipeline.PipelineHandler;

public class ResponseDeliveryHandler implements PipelineHandler {
    @Override
    public void handle(PipelineContext ctx) {
        if (ctx.getHandlerResult() != null) {
            var responsePacket = ctx.getHandlerResult().responsePacket();
            responsePacket.ifPresent(packet -> {
                ctx.getBrokerContext().getMessageDeliveryService()
                        .send(ctx.getClientChannel(), packet);
            });
        }
    }
}
