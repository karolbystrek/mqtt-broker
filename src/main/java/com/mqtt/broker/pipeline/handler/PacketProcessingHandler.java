package com.mqtt.broker.pipeline.handler;

import com.mqtt.broker.handler.MqttPacketHandler;
import com.mqtt.broker.pipeline.PipelineContext;
import com.mqtt.broker.pipeline.PipelineHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class PacketProcessingHandler implements PipelineHandler {
    private final MqttPacketHandler handlerFactory;

    @Override
    public void handle(PipelineContext ctx) {
        try {
            var result = handlerFactory.handle(ctx.getClientChannel(), ctx.getPacket());
            ctx.setHandlerResult(result);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
