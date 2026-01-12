package com.mqtt.broker.trie;

import com.fasterxml.jackson.annotation.JsonCreator;

import static java.util.Objects.requireNonNull;

public record TopicPath(String original, String[] levels) {

    private static final String SINGLE_LEVEL_WILDCARD = "+";
    private static final String MULTI_LEVEL_WILDCARD = "#";
    private static final String TOPIC_LEVEL_SEPARATOR = "/";
    private static final int MAX_TOPIC_LENGTH = 65535;

    @JsonCreator
    public static TopicPath parse(String topic) {
        validateTopic(topic);
        return new TopicPath(topic, topic.split(TOPIC_LEVEL_SEPARATOR, -1));
    }

    private static void validateTopic(String topic) {
        requireNonNull(topic, "Topic filter cannot be null");

        if (topic.isEmpty()) {
            throw new IllegalArgumentException("Topic filter cannot be empty");
        }

        if (topic.length() > MAX_TOPIC_LENGTH) {
            throw new IllegalArgumentException("Topic filter exceeds maximum length of " + MAX_TOPIC_LENGTH);
        }

        validateMultiLevelWildcard(topic);
        validateSingleLevelWildcards(topic);
    }

    private static void validateMultiLevelWildcard(String topic) {
        int multiLevelIndex = topic.indexOf(MULTI_LEVEL_WILDCARD);

        if (multiLevelIndex == -1) {
            return;
        }

        if (multiLevelIndex != topic.length() - 1) {
            throw new IllegalArgumentException("Multi-level wildcard '#' must be at the end of the topic filter");
        }

        if (multiLevelIndex > 0 && topic.charAt(multiLevelIndex - 1) != '/') {
            throw new IllegalArgumentException("Multi-level wildcard '#' must be preceded by a topic level separator '/'");
        }
    }

    private static void validateSingleLevelWildcards(String topic) {
        String[] levels = topic.split(TOPIC_LEVEL_SEPARATOR, -1);

        for (String level : levels) {
            if (level.contains(SINGLE_LEVEL_WILDCARD) && !level.equals(SINGLE_LEVEL_WILDCARD)) {
                throw new IllegalArgumentException("Single-level wildcard '+' must occupy an entire topic level");
            }
        }
    }
}
