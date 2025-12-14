package com.mqtt.broker.pipeline.handler;

import com.mqtt.broker.event.BrokerEventPublisher;
import com.mqtt.broker.pipeline.PipelineContext;
import com.mqtt.broker.pipeline.PipelineHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventPublishingHandler implements PipelineHandler {
    private final BrokerEventPublisher eventPublisher;

    @Override
    public void handle(PipelineContext ctx) {
        if (ctx.getHandlerResult() != null) {
            ctx.getHandlerResult().event().ifPresent(eventPublisher::publish);
        }
    }
}
