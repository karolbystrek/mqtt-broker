package com.mqtt.broker.event;

import com.mqtt.broker.Session;
import java.nio.channels.SocketChannel;

public record ClientConnectedEvent(SocketChannel channel, Session session) implements BrokerEvent {}
