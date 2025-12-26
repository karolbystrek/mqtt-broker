package com.mqtt.broker.trie.strategy;

import com.mqtt.broker.trie.TrieNode;

@FunctionalInterface
public interface TrieStrategy<T> {

    String SINGLE_LEVEL_WILDCARD = "+";
    String MULTI_LEVEL_WILDCARD = "#";

    void visit(TrieNode<T> node);
}
