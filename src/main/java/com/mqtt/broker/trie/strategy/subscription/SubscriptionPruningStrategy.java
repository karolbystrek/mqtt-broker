package com.mqtt.broker.trie.strategy.subscription;

import com.mqtt.broker.trie.TrieNode;
import com.mqtt.broker.trie.strategy.TrieStrategy;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class SubscriptionPruningStrategy implements TrieStrategy<Set<String>> {

    private final String clientId;

    @Override
    public void visit(TrieNode<Set<String>> node) {
        if (node.getValue() != null) {
            node.getValue().remove(clientId);
        }

        for (TrieNode<Set<String>> child : node.children().values()) {
            child.perform(this);
        }
    }
}
