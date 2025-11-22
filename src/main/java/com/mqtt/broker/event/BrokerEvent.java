package com.mqtt.broker.event;

import java.nio.channels.SocketChannel;

public sealed interface BrokerEvent permits ClientConnectedEvent, ClientSubscribedEvent, CloseConnectionEvent, PublishEvent, ConnectionLostEvent {
    SocketChannel channel();
}

