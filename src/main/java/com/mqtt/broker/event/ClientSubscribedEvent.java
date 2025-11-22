package com.mqtt.broker.event;

import java.nio.channels.SocketChannel;
import java.util.List;

public record ClientSubscribedEvent(SocketChannel channel, List<String> topicFilters) implements BrokerEvent {}
