package com.mqtt.broker.event;

import com.mqtt.broker.packet.PublishPacket;
import java.nio.channels.SocketChannel;

public record PublishEvent(SocketChannel channel, PublishPacket packet) implements BrokerEvent {
}
