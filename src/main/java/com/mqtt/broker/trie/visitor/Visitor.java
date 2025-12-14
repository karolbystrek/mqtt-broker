package com.mqtt.broker.trie.visitor;

import com.mqtt.broker.trie.TrieNode;

@FunctionalInterface
public interface Visitor {

    String SINGLE_LEVEL_WILDCARD = "+";
    String MULTI_LEVEL_WILDCARD = "#";

    void visit(TrieNode node);
}
