package com.mqtt.broker.repository;

import com.mqtt.broker.trie.TopicPath;
import com.mqtt.broker.trie.TopicTree;
import com.mqtt.broker.trie.strategy.subscription.SubscriberLookupStrategy;
import com.mqtt.broker.trie.strategy.subscription.SubscriptionInsertionStrategy;
import com.mqtt.broker.trie.strategy.subscription.SubscriptionPruningStrategy;
import com.mqtt.broker.trie.strategy.subscription.SubscriptionRemovalStrategy;

import java.util.Set;

public class SubscriptionRepository {

    private final TopicTree<Set<String>> tree = new TopicTree<>();

    public void add(String clientId, TopicPath topicPath) {
        var strategy = new SubscriptionInsertionStrategy(topicPath.levels(), clientId);
        tree.perform(strategy);
    }

    public void remove(String clientId, TopicPath topicPath) {
        var strategy = new SubscriptionRemovalStrategy(topicPath.levels(), clientId);
        tree.perform(strategy);
    }

    public void removeForClient(String clientId) {
        var strategy = new SubscriptionPruningStrategy(clientId);
        tree.perform(strategy);
    }

    public void findSubscribers(TopicPath topicPath, Set<String> subscriberSet) {
        var strategy = new SubscriberLookupStrategy(topicPath.levels(), subscriberSet);
        tree.perform(strategy);
    }
}
