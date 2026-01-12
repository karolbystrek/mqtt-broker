package com.mqtt.broker.trie.strategy.authorization;

import com.mqtt.broker.auth.AuthorizationEntry;
import com.mqtt.broker.trie.TrieNode;
import com.mqtt.broker.trie.strategy.TrieStrategy;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class AuthorizationLookupStrategy implements TrieStrategy<Set<AuthorizationEntry>> {

    private final String[] levels;
    private final Set<AuthorizationEntry> matchingEntries;

    private int levelIndex = 0;

    @Override
    public void visit(TrieNode<Set<AuthorizationEntry>> node) {
        // Check for '#' wildcard at this level (Permission defined on # covers everything)
        TrieNode<Set<AuthorizationEntry>> multiLevelWildcardNode = node.children().get(MULTI_LEVEL_WILDCARD);
        if (multiLevelWildcardNode != null && multiLevelWildcardNode.getValue() != null) {
            matchingEntries.addAll(multiLevelWildcardNode.getValue());
        }

        // Reached the end of the topic (resource) levels
        if (levelIndex == levels.length) {
            if (node.getValue() != null) {
                matchingEntries.addAll(node.getValue());
            }
            // Logic note: If user has ANY permission on a parent node using wildcard, it's captured above.
            // If the permission is specific (exact match), it's captured here.
            return;
        }

        String currentLevel = levels[levelIndex];
        int currentIndex = levelIndex;

        // Explore the '+' wildcard path (Permission defined on + covers this level)
        TrieNode<Set<AuthorizationEntry>> singleLevelWildcardNode = node.children().get(SINGLE_LEVEL_WILDCARD);
        if (singleLevelWildcardNode != null) {
            levelIndex = currentIndex + 1;
            singleLevelWildcardNode.perform(this);
        }

        // Explore the exact match path
        TrieNode<Set<AuthorizationEntry>> exactMatchNode = node.children().get(currentLevel);
        if (exactMatchNode != null) {
            levelIndex = currentIndex + 1;
            exactMatchNode.perform(this);
        }

        // Restore index
        levelIndex = currentIndex;
    }
}
