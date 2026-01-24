package com.mqtt.broker.pipeline.interceptor;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.pipeline.ProcessingResult;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;
import java.util.Optional;

@Slf4j
public abstract class ChainablePacketInterceptor implements Interceptor {

    protected Interceptor next;

    @Override
    public void setNext(Interceptor next) {
        this.next = next;
    }

    @Override
    public ProcessingResult intercept(SocketChannel channel, MqttPacket packet) {
        Optional<ProcessingResult> result = process(channel, packet);

        if (result.isPresent()) {
            return result.get();
        }

        if (next != null) {
            return next.intercept(channel, packet);
        }

        return ProcessingResult.empty();
    }

    /**
     * @return Optional.of(result) to short-circuit, or Optional.empty() to proceed.
     */
    protected abstract Optional<ProcessingResult> process(SocketChannel channel, MqttPacket packet);
}
