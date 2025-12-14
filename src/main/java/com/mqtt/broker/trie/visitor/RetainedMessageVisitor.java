package com.mqtt.broker.trie.visitor;

import com.mqtt.broker.trie.RetainedMessage;
import com.mqtt.broker.trie.RetainedMessageWithTopic;
import com.mqtt.broker.trie.TrieNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RetainedMessageVisitor implements Visitor {

    private final String[] levels;
    private final List<RetainedMessageWithTopic> retainedMessages;

    private int levelIndex = 0;
    private String currentPath = "";

    @Override
    public void visit(TrieNode node) {
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
            for (var entry : node.getChildren().entrySet()) {
                String childName = entry.getKey();
                if (currentIndex == 0 && childName.startsWith("$")) {
                    continue;
                }

                // Update state for child
                levelIndex = currentIndex + 1;
                currentPath = savedPath.isEmpty() ? childName : savedPath + "/" + childName;

                entry.getValue().accept(this);
            }
            // Restore state
            levelIndex = currentIndex;
            currentPath = savedPath;
            return;
        }

        TrieNode childNode = node.getChildren().get(level);
        if (childNode != null) {
            levelIndex = currentIndex + 1;
            currentPath = savedPath.isEmpty() ? level : savedPath + "/" + level;

            childNode.accept(this);

            // Restore state
            levelIndex = currentIndex;
            currentPath = savedPath;
        }
    }

    private void findAllRetainedMessages(TrieNode node, String path) {
        appendRetainedMessage(node, path);

        for (var entry : node.getChildren().entrySet()) {
            String nextPath = path.isEmpty() ? entry.getKey() : path + "/" + entry.getKey();
            findAllRetainedMessages(entry.getValue(), nextPath);
        }
    }

    private void appendRetainedMessage(TrieNode node, String topicName) {
        RetainedMessage retained = node.getRetainedMessage();
        if (retained == null) {
            return;
        }
        log.info("Found retained message for topic: {}", topicName);
        retainedMessages.add(new RetainedMessageWithTopic(retained, topicName));
    }
}
