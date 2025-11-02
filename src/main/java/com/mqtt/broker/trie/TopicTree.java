package com.mqtt.broker.trie;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.mqtt.broker.trie.TopicFilterValidator.validateTopicFilter;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;

public class TopicTree {

    private final TrieNode root = new TrieNode();

    private static final String SINGLE_LEVEL_WILDCARD = "+";
    private static final String MULTI_LEVEL_WILDCARD = "#";
    private static final String TOPIC_LEVEL_SEPARATOR = "/";

    public void subscribeTo(String topic, String clientId) {
        requireNonNull(clientId, "Client ID cannot be null");
        validateTopicFilter(topic);


        String[] levels = topic.split(TOPIC_LEVEL_SEPARATOR);
        TrieNode currentNode = root;
        for (String level : levels) {
            currentNode = currentNode.getChildren()
                    .computeIfAbsent(level, k -> new TrieNode());
        }
        currentNode.getSubscribers().add(clientId);
    }

    public void unsubscribeFrom(String topic, String clientId) {
        requireNonNull(clientId, "Client ID cannot be null");
        validateTopicFilter(topic);

        String[] levels = topic.split(TOPIC_LEVEL_SEPARATOR);
        TrieNode currentNode = root;

        for (String level : levels) {
            currentNode = currentNode.getChildren().get(level);
            if (currentNode == null) {
                return; // topic not found
            }
        }

        currentNode.getSubscribers().remove(clientId);
    }

    public void removeAllSubscriptionsFor(String clientId) {
        requireNonNull(clientId, "Client ID cannot be null");
        removeClientFromNode(root, clientId);
    }

    private void removeClientFromNode(TrieNode node, String clientId) {
        node.getSubscribers().remove(clientId);

        node.getChildren().values()
                .forEach(childNode -> removeClientFromNode(childNode, clientId));
    }

    public Set<String> getSubscribersFor(String topic) {
        requireNonNull(topic, "Topic cannot be null");

        if (topic.isEmpty()) {
            return emptySet();
        }

        Set<String> matchingSubscribers = new CopyOnWriteArraySet<>();
        String[] levels = topic.split(TOPIC_LEVEL_SEPARATOR);
        findMatchingSubscribers(root, levels, 0, matchingSubscribers);
        return unmodifiableSet(matchingSubscribers);
    }

    private void findMatchingSubscribers(TrieNode node, String[] levels, int levelIndex, Set<String> matchingSubscribers) {
        // check for '#' wildcard at this level
        TrieNode multiLevelWildcardNode = node.getChildren().get(MULTI_LEVEL_WILDCARD);
        if (multiLevelWildcardNode != null) {
            matchingSubscribers.addAll(multiLevelWildcardNode.getSubscribers());
        }

        // reached the end of the topic levels
        if (levelIndex == levels.length) {
            matchingSubscribers.addAll(node.getSubscribers());
            return;
        }

        String currentLevel = levels[levelIndex];

        // explore the '+' wildcard path
        TrieNode singleLevelWildcardNode = node.getChildren().get(SINGLE_LEVEL_WILDCARD);
        if (singleLevelWildcardNode != null) {
            findMatchingSubscribers(singleLevelWildcardNode, levels, levelIndex + 1, matchingSubscribers);
        }

        // explore the exact match path
        TrieNode exactMatchNode = node.getChildren().get(currentLevel);
        if (exactMatchNode != null) {
            findMatchingSubscribers(exactMatchNode, levels, levelIndex + 1, matchingSubscribers);
        }
    }
}
