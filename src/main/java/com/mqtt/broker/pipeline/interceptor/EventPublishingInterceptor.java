package com.mqtt.broker.pipeline.interceptor;

import com.mqtt.broker.event.EventPublisher;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.pipeline.ProcessingResult;
import lombok.RequiredArgsConstructor;

import java.nio.channels.SocketChannel;

@RequiredArgsConstructor
public class EventPublishingInterceptor implements Interceptor {

    private final EventPublisher eventPublisher;
    private Interceptor next;

    @Override
    public void setNext(Interceptor next) {
        this.next = next;
    }

    @Override
    public ProcessingResult intercept(SocketChannel channel, MqttPacket packet) {
        ProcessingResult result = (next != null)
                ? next.intercept(channel, packet)
                : ProcessingResult.empty();

        result.event().ifPresent(eventPublisher::publish);

        return result;
    }
}
