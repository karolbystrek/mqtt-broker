package com.mqtt.broker.pipeline;

public interface PipelineHandler {
    void handle(PipelineContext ctx);
}
