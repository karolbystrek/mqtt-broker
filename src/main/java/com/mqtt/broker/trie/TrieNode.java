package com.mqtt.broker.trie;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Getter
class TrieNode {
    private final ConcurrentHashMap<String, TrieNode> children;
    private final Set<String> subscribers;
    @Setter private RetainedMessage retainedMessage;

    public TrieNode() {
        this.children = new ConcurrentHashMap<>();
        this.subscribers = new CopyOnWriteArraySet<>();
    }
}
