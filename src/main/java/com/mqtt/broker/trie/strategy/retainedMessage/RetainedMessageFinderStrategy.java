package com.mqtt.broker.trie.strategy.retainedMessage;

import com.mqtt.broker.trie.TrieNode;
import com.mqtt.broker.trie.strategy.TrieStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RetainedMessageFinderStrategy implements TrieStrategy<RetainedMessage> {

    private final String[] levels;
    private final List<RetainedMessageWithTopic> retainedMessages;

    private int levelIndex = 0;
    private String currentPath = "";

    @Override
    public void visit(TrieNode<RetainedMessage> node) {
        if (levelIndex == levels.length) {
            appendRetainedMessage(node, currentPath);
            return;
        }

        String level = levels[levelIndex];

        // Store state to restore
        int currentIndex = levelIndex;
        String savedPath = currentPath;

        if (MULTI_LEVEL_WILDCARD.equals(level)) {
            findAllRetainedMessages(node, currentPath);
            return; // findAll is self-contained recursion
        }

        if (SINGLE_LEVEL_WILDCARD.equals(level)) {
            for (var entry : node.children().entrySet()) {
                String childName = entry.getKey();
                if (currentIndex == 0 && childName.startsWith("$")) {
                    continue;
                }

                // Update state for child
                levelIndex = currentIndex + 1;
                currentPath = savedPath.isEmpty() ? childName : savedPath + "/" + childName;

                entry.getValue().perform(this);
            }
            // Restore state
            levelIndex = currentIndex;
            currentPath = savedPath;
            return;
        }

        TrieNode<RetainedMessage> childNode = node.children().get(level);
        if (childNode != null) {
            levelIndex = currentIndex + 1;
            currentPath = savedPath.isEmpty() ? level : savedPath + "/" + level;

            childNode.perform(this);

            // Restore state
            levelIndex = currentIndex;
            currentPath = savedPath;
        }
    }

    private void findAllRetainedMessages(TrieNode<RetainedMessage> node, String path) {
        appendRetainedMessage(node, path);

        for (var entry : node.children().entrySet()) {
            String nextPath = path.isEmpty() ? entry.getKey() : path + "/" + entry.getKey();
            findAllRetainedMessages(entry.getValue(), nextPath);
        }
    }

    private void appendRetainedMessage(TrieNode<RetainedMessage> node, String topicName) {
        RetainedMessage retained = node.getValue();
        if (retained == null) {
            return;
        }
        log.info("Found retained message for topic: {}", topicName);
        retainedMessages.add(new RetainedMessageWithTopic(retained, topicName));
    }
}
