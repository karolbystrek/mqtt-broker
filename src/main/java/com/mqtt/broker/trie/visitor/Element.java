package com.mqtt.broker.trie.visitor;

@FunctionalInterface
public interface Element {

    void accept(Visitor visitor);
}
