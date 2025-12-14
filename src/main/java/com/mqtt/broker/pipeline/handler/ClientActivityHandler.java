package com.mqtt.broker.pipeline.handler;

import com.mqtt.broker.Session;
import com.mqtt.broker.pipeline.PipelineContext;
import com.mqtt.broker.pipeline.PipelineHandler;

public class ClientActivityHandler implements PipelineHandler {
    @Override
    public void handle(PipelineContext ctx) {
        Session session = ctx.getBrokerContext().getSession(ctx.getClientChannel());
        if (session != null) {
            session.updateLastActivity();
        }
    }
}
