package com.mqtt.broker.pipeline;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class Pipeline {
    private final List<PipelineHandler> handlers;

    public void execute(PipelineContext ctx) {
        for (PipelineHandler handler : handlers) {
            handler.handle(ctx);
            if (ctx.isTerminated()) {
                break;
            }
        }
    }
}
