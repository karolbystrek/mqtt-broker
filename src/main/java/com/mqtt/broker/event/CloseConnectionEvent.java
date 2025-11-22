package com.mqtt.broker.event;

import java.nio.channels.SocketChannel;

public record CloseConnectionEvent(SocketChannel channel) implements BrokerEvent {
}
