package com.mqtt.broker.pipeline.handler;

import com.mqtt.broker.pipeline.PipelineContext;
import com.mqtt.broker.pipeline.PipelineHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingHandler implements PipelineHandler {
    @Override
    public void handle(PipelineContext ctx) {
        log.info("Received packet: {} from client: {}",
                ctx.getPacket().getFixedHeader().packetType(),
                ctx.getClientChannel().socket().getRemoteSocketAddress());
    }
}
