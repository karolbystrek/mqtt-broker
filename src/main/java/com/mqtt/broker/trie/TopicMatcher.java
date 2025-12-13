package com.mqtt.broker.trie;

public class TopicMatcher {

    public static boolean matches(String permissionPattern, String topic) {
        if (permissionPattern.equals(topic)) {
            return true;
        }
        if (permissionPattern.equals("#")) {
            return true;
        }
        if (permissionPattern.equals("+")) {
            return !topic.contains("/");
        }

        String[] patternLevels = permissionPattern.split("/");
        String[] topicLevels = topic.split("/");

        for (int i = 0; i < patternLevels.length; i++) {
            String left = patternLevels[i];

            if (i >= topicLevels.length) {
                return false;
            }
            String right = topicLevels[i];

            if (left.equals("#")) {
                return true;
            }
            if (!left.equals("+") && !left.equals(right)) {
                return false;
            }
        }

        return patternLevels.length == topicLevels.length;
    }
}

