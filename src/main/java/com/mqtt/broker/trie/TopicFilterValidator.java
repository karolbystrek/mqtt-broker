package com.mqtt.broker.trie;

import static java.util.Objects.requireNonNull;

public class TopicFilterValidator {

    private static final String SINGLE_LEVEL_WILDCARD = "+";
    private static final String MULTI_LEVEL_WILDCARD = "#";
    private static final String TOPIC_LEVEL_SEPARATOR = "/";
    private static final int MAX_TOPIC_LENGTH = 65535;

    public static void validateTopicFilter(String topicFilter) {
        requireNonNull(topicFilter, "Topic filter cannot be null");

        if (topicFilter.isEmpty()) {
            throw new IllegalArgumentException("Topic filter cannot be empty");
        }

        if (topicFilter.length() > MAX_TOPIC_LENGTH) {
            throw new IllegalArgumentException("Topic filter exceeds maximum length of " + MAX_TOPIC_LENGTH);
        }

        validateMultiLevelWildcard(topicFilter);
        validateSingleLevelWildcards(topicFilter);
    }

    private static void validateMultiLevelWildcard(String topicFilter) {
        int multiLevelIndex = topicFilter.indexOf(MULTI_LEVEL_WILDCARD);

        if (multiLevelIndex == -1) {
            return;
        }

        if (multiLevelIndex != topicFilter.length() - 1) {
            throw new IllegalArgumentException("Multi-level wildcard '#' must be at the end of the topic filter");
        }

        if (multiLevelIndex > 0 && topicFilter.charAt(multiLevelIndex - 1) != '/') {
            throw new IllegalArgumentException("Multi-level wildcard '#' must be preceded by a topic level separator '/'");
        }
    }

    private static void validateSingleLevelWildcards(String topicFilter) {
        String[] levels = topicFilter.split(TOPIC_LEVEL_SEPARATOR, -1);

        for (String level : levels) {
            if (level.contains(SINGLE_LEVEL_WILDCARD) && !level.equals(SINGLE_LEVEL_WILDCARD)) {
                throw new IllegalArgumentException("Single-level wildcard '+' must occupy an entire topic level");
            }
        }
    }

    private TopicFilterValidator() {
    }
}
