package com.mqtt.broker.trie.strategy;

import com.mqtt.broker.trie.TrieNode;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class SubscriptionRemovalStrategy implements TrieStrategy<Set<String>> {

    private final String[] levels;
    private final String clientId;
    private int currentLevel = 0;

    @Override
    public void visit(TrieNode<Set<String>> node) {
        if (currentLevel == levels.length) {
            if (node.getValue() != null) {
                node.getValue().remove(clientId);
            }
            return;
        }

        String level = levels[currentLevel];
        TrieNode<Set<String>> child = node.children().get(level);

        if (child != null) {
            currentLevel++;
            child.perform(this);
        }
    }
}
