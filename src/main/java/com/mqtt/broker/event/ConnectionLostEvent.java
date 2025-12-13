package com.mqtt.broker.event;

import java.nio.channels.SocketChannel;

public record ConnectionLostEvent(SocketChannel channel) implements BrokerEvent {
}
