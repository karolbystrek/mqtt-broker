package com.mqtt.broker.trie;

public record RetainedMessageWithTopic(RetainedMessage message, String topic) {
}
