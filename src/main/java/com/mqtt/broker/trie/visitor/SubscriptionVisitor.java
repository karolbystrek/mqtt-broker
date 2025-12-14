package com.mqtt.broker.trie.visitor;

import com.mqtt.broker.trie.TrieNode;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class SubscriptionVisitor implements Visitor {

    private final String[] levels;
    private final Set<String> matchingSubscribers;

    private int levelIndex = 0;

    @Override
    public void visit(TrieNode node) {
        // Check for '#' wildcard at this level
        TrieNode multiLevelWildcardNode = node.getChildren().get(MULTI_LEVEL_WILDCARD);
        if (multiLevelWildcardNode != null) {
            matchingSubscribers.addAll(multiLevelWildcardNode.getSubscribers());
        }

        // Reached the end of the topic levels
        if (levelIndex == levels.length) {
            matchingSubscribers.addAll(node.getSubscribers());
            return;
        }

        String currentLevel = levels[levelIndex];

        // Store current index to restore after recursion
        int currentIndex = levelIndex;

        // Explore the '+' wildcard path
        TrieNode singleLevelWildcardNode = node.getChildren().get(SINGLE_LEVEL_WILDCARD);
        if (singleLevelWildcardNode != null) {
            levelIndex = currentIndex + 1;
            singleLevelWildcardNode.accept(this);
        }

        // Explore the exact match path
        TrieNode exactMatchNode = node.getChildren().get(currentLevel);
        if (exactMatchNode != null) {
            levelIndex = currentIndex + 1;
            exactMatchNode.accept(this);
        }

        // Restore index
        levelIndex = currentIndex;
    }
}
