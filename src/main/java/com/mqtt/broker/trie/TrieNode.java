package com.mqtt.broker.trie;

import com.mqtt.broker.trie.visitor.Element;
import com.mqtt.broker.trie.visitor.Visitor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
public class TrieNode implements Element {
    private final ConcurrentHashMap<String, TrieNode> children;
    private final Set<String> subscribers;
    @Setter
    private RetainedMessage retainedMessage;

    public TrieNode() {
        this.children = new ConcurrentHashMap<>();
        this.subscribers = new CopyOnWriteArraySet<>();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
