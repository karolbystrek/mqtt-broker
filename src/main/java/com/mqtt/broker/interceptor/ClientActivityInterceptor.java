package com.mqtt.broker.interceptor;

import com.mqtt.broker.BrokerContext;
import com.mqtt.broker.Session;
import com.mqtt.broker.packet.MqttPacket;
import lombok.RequiredArgsConstructor;

import java.nio.channels.SocketChannel;
import java.util.Optional;

@RequiredArgsConstructor
public class ClientActivityInterceptor extends ChainablePacketInterceptor {

    private final BrokerContext context;

    @Override
    protected Optional<ProcessingResult> process(SocketChannel channel, MqttPacket packet) {
        Session session = context.getSession(channel);
        if (session != null) {
            session.updateLastActivity();
        }
        return Optional.empty(); // Proceed to next interceptor
    }
}
