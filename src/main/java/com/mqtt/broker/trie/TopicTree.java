package com.mqtt.broker.trie;

import com.mqtt.broker.trie.visitor.Visitor;
import lombok.Getter;

@Getter
public class TopicTree<T> {

    private final TrieNode<T> root = new TrieNode<>();

    public void accept(Visitor<T> visitor) {
        root.accept(visitor);
    }
}
