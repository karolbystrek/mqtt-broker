package com.mqtt.broker.repository;

import com.mqtt.broker.trie.TopicPath;
import com.mqtt.broker.trie.TopicTree;
import com.mqtt.broker.trie.strategy.retainedMessage.RetainedMessage;
import com.mqtt.broker.trie.strategy.retainedMessage.RetainedMessageFinderStrategy;
import com.mqtt.broker.trie.strategy.retainedMessage.RetainedMessageLookupStrategy;
import com.mqtt.broker.trie.strategy.retainedMessage.RetainedMessageWithTopic;

import java.util.List;

public class RetainedMessageRepository {

    private final TopicTree<RetainedMessage> tree = new TopicTree<>();

    public void add(TopicPath topicPath, RetainedMessage message) {
        var strategy = new RetainedMessageLookupStrategy(topicPath.levels(), message);
        tree.perform(strategy);
    }

    public void find(TopicPath topicPath, List<RetainedMessageWithTopic> retainedMessages) {
        var strategy = new RetainedMessageFinderStrategy(topicPath.levels(), retainedMessages);
        tree.perform(strategy);
    }
}
