package com.mqtt.broker.trie.visitor;

import com.mqtt.broker.auth.AuthorizationEntry;
import com.mqtt.broker.trie.TrieNode;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RequiredArgsConstructor
public class AuthorizationAddVisitor implements Visitor<Set<AuthorizationEntry>> {

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
        child.accept(this);
    }
}
