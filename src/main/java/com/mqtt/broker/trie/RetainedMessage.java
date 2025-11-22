package com.mqtt.broker.trie;

import com.mqtt.broker.packet.MqttQoS;

public record RetainedMessage(byte[] payload, MqttQoS qos) {
}
