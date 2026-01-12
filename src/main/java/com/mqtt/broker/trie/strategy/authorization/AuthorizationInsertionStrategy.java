package com.mqtt.broker.trie.strategy.authorization;

import com.mqtt.broker.auth.AuthorizationEntry;
import com.mqtt.broker.trie.TrieNode;
import com.mqtt.broker.trie.strategy.TrieStrategy;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public class AuthorizationInsertionStrategy implements TrieStrategy<Set<AuthorizationEntry>> {

    private final String[] levels;
    private final AuthorizationEntry entry;
    private int currentLevel = 0;

    @Override
    public void visit(TrieNode<Set<AuthorizationEntry>> node) {
        if (currentLevel == levels.length) {
            if (node.getValue() == null) {
                node.setValue(new CopyOnWriteArraySet<>());
            }
            node.getValue().add(entry);
            return;
        }

        String level = levels[currentLevel];
        TrieNode<Set<AuthorizationEntry>> child = node.children().computeIfAbsent(level, k -> new TrieNode<>());

        currentLevel++;
        child.perform(this);
    }
}
