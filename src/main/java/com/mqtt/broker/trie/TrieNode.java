package com.mqtt.broker.trie;

import com.mqtt.broker.trie.visitor.Visitor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class TrieNode<T> {

    private final Map<String, TrieNode<T>> children;
    private volatile T value;

    public TrieNode() {
        this(new ConcurrentHashMap<>(), null);
    }

    public TrieNode(Map<String, TrieNode<T>> children, T value) {
        this.children = children;
        this.value = value;
    }

    public Map<String, TrieNode<T>> children() {
        return children;
    }

    public void accept(Visitor<T> visitor) {
        visitor.visit(this);
    }
}
