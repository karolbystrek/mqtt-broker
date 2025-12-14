package com.mqtt.broker.trie.visitor;

import com.mqtt.broker.trie.RetainedMessage;
import com.mqtt.broker.trie.TrieNode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RetainedMessageAddVisitor implements Visitor<RetainedMessage> {

    private final String[] levels;
    private final RetainedMessage retainedMessage;
    private int currentLevel = 0;

    @Override
    public void visit(TrieNode<RetainedMessage> node) {
        if (currentLevel == levels.length) {
            node.setValue(retainedMessage);
            return;
        }

        String level = levels[currentLevel];
        TrieNode<RetainedMessage> child = node.children().computeIfAbsent(level, k -> new TrieNode<>());
        
        currentLevel++;
        child.accept(this);
    }
}
