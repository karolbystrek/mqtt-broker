package com.mqtt.broker.event;

public sealed interface BrokerEvent permits ClientConnectedEvent, ClientSubscribedEvent, CloseConnectionEvent, PublishEvent, ConnectionLostEvent {
}

