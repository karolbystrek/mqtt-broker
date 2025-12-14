package com.mqtt.broker.trie.visitor;

import com.mqtt.broker.trie.TrieNode;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class SubscriptionCleanupVisitor implements Visitor<Set<String>> {

    private final String clientId;

    @Override
    public void visit(TrieNode<Set<String>> node) {
        if (node.getValue() != null) {
            node.getValue().remove(clientId);
        }
        
        for (TrieNode<Set<String>> child : node.children().values()) {
            child.accept(this);
        }
    }
}
