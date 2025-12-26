package com.mqtt.broker.trie.strategy;

import com.mqtt.broker.trie.TrieNode;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class SubscriberLookupStrategy implements TrieStrategy<Set<String>> {

    private final String[] levels;
    private final Set<String> matchingSubscribers;

    private int levelIndex = 0;

    @Override
    public void visit(TrieNode<Set<String>> node) {
        // Check for '#' wildcard at this level
        TrieNode<Set<String>> multiLevelWildcardNode = node.children().get(MULTI_LEVEL_WILDCARD);
        if (multiLevelWildcardNode != null && multiLevelWildcardNode.getValue() != null) {
            matchingSubscribers.addAll(multiLevelWildcardNode.getValue());
        }

        // Reached the end of the topic levels
        if (levelIndex == levels.length) {
            if (node.getValue() != null) {
                matchingSubscribers.addAll(node.getValue());
            }
            return;
        }

        String currentLevel = levels[levelIndex];

        // Store current index to restore after recursion
        int currentIndex = levelIndex;

        // Explore the '+' wildcard path
        TrieNode<Set<String>> singleLevelWildcardNode = node.children().get(SINGLE_LEVEL_WILDCARD);
        if (singleLevelWildcardNode != null) {
            levelIndex = currentIndex + 1;
            singleLevelWildcardNode.perform(this);
        }

        // Explore the exact match path
        TrieNode<Set<String>> exactMatchNode = node.children().get(currentLevel);
        if (exactMatchNode != null) {
            levelIndex = currentIndex + 1;
            exactMatchNode.perform(this);
        }

        // Restore index
        levelIndex = currentIndex;
    }
}
