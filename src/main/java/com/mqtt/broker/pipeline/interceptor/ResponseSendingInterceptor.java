package com.mqtt.broker.pipeline.interceptor;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.pipeline.ProcessingResult;
import lombok.RequiredArgsConstructor;

import java.nio.channels.SocketChannel;

@RequiredArgsConstructor
public class ResponseSendingInterceptor implements Interceptor {

    private final BrokerContext context;
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

        result.responsePacket().ifPresent(response ->
                context.getMessageDeliveryService().send(channel, response)
        );

        return result;
    }
}
