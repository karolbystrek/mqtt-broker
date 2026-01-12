package com.mqtt.broker.trie.strategy.retainedMessage;

public record RetainedMessageWithTopic(RetainedMessage message, String topic) {
}
