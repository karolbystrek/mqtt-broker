package com.mqtt.broker.pipeline;

import com.mqtt.broker.packet.MqttPacket;
import com.mqtt.broker.pipeline.interceptor.Interceptor;
import lombok.RequiredArgsConstructor;

import java.nio.channels.SocketChannel;

@RequiredArgsConstructor
public class PacketProcessingPipeline implements Pipeline {

    private final Interceptor head;

    public ProcessingResult process(SocketChannel channel, MqttPacket packet) {
        if (head == null) {
            return ProcessingResult.empty();
        }
        return head.intercept(channel, packet);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Interceptor head;
        private Interceptor tail;

        public Builder addInterceptor(Interceptor interceptor) {
            if (head == null) {
                head = interceptor;
                tail = interceptor;
            } else {
                tail.setNext(interceptor);
                tail = interceptor;
            }
            return this;
        }

        public PacketProcessingPipeline build() {
            return new PacketProcessingPipeline(head);
        }
    }
}
