package com.mqtt.broker.trie.strategy.retainedMessage;

import com.mqtt.broker.packet.MqttQoS;

public record RetainedMessage(byte[] payload, MqttQoS qos) {
}
