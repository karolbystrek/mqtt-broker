package com.mqtt.broker.event;

import com.mqtt.broker.session.Session;

import java.nio.channels.SocketChannel;

public record ClientConnectedEvent(SocketChannel channel, Session session) implements BrokerEvent {
}
