package com.mqtt.broker.interceptor;

import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

@Slf4j
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
