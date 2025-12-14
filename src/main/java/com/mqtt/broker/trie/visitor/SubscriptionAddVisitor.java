package com.mqtt.broker.trie.visitor;

import com.mqtt.broker.trie.TrieNode;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public class SubscriptionAddVisitor implements Visitor<Set<String>> {

    private final String[] levels;
    private final String clientId;
    private int currentLevel = 0;

    @Override
    public void visit(TrieNode<Set<String>> node) {
        if (currentLevel == levels.length) {
            if (node.getValue() == null) {
                node.setValue(new CopyOnWriteArraySet<>());
            }
            node.getValue().add(clientId);
            return;
        }

        String level = levels[currentLevel];
        TrieNode<Set<String>> child = node.children().computeIfAbsent(level, k -> new TrieNode<>());
        
        currentLevel++;
        child.accept(this);
    }
}
