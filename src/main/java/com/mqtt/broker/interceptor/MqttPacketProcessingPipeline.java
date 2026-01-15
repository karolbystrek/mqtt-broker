package com.mqtt.broker.interceptor;

import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;

@Slf4j
@RequiredArgsConstructor
public class MqttPacketProcessingPipeline {

    private final PacketInterceptor head;

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
        private PacketInterceptor head;
        private PacketInterceptor tail;

        public Builder addInterceptor(PacketInterceptor interceptor) {
            if (head == null) {
                head = interceptor;
                tail = interceptor;
            } else {
                tail.setNext(interceptor);
                tail = interceptor;
            }
            return this;
        }

        public MqttPacketProcessingPipeline build() {
            return new MqttPacketProcessingPipeline(head);
        }
    }
}
