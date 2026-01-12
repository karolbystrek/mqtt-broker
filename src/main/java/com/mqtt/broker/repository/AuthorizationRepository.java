package com.mqtt.broker.repository;

import com.mqtt.broker.auth.AuthorizationEntry;
import com.mqtt.broker.trie.TopicPath;
import com.mqtt.broker.trie.TopicTree;
import com.mqtt.broker.trie.strategy.authorization.AuthorizationInsertionStrategy;
import com.mqtt.broker.trie.strategy.authorization.AuthorizationLookupStrategy;

import java.util.Set;

public class AuthorizationRepository {

    private final TopicTree<Set<AuthorizationEntry>> tree = new TopicTree<>();

    public void add(TopicPath topicPath, AuthorizationEntry entry) {
        var strategy = new AuthorizationInsertionStrategy(topicPath.levels(), entry);
        tree.perform(strategy);
    }

    public void find(TopicPath topicPath, Set<AuthorizationEntry> entries) {
        var strategy = new AuthorizationLookupStrategy(topicPath.levels(), entries);
        tree.perform(strategy);
    }
}
