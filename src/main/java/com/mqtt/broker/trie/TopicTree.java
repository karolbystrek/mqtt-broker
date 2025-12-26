package com.mqtt.broker.trie;

import com.mqtt.broker.trie.strategy.TrieStrategy;
import lombok.Getter;

@Getter
public class TopicTree<T> {

    private final TrieNode<T> root = new TrieNode<>();

    public void perform(TrieStrategy<T> strategy) {
        root.perform(strategy);
    }
}
