package com.mqtt.broker.trie;

import com.mqtt.broker.packet.MqttQoS;
import com.mqtt.broker.trie.visitor.RetainedMessageVisitor;
import com.mqtt.broker.trie.visitor.SubscriptionVisitor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.mqtt.broker.trie.TopicValidator.validateTopic;
import static java.util.Collections.*;

@Slf4j
public class TopicTree {

    private final TrieNode root = new TrieNode();

    private static final String TOPIC_LEVEL_SEPARATOR = "/";

    public void subscribeTo(String topic, String clientId) {
        if (topic == null || topic.isEmpty()) {
            return;
        }
        validateTopic(topic);

        String[] levels = topic.split(TOPIC_LEVEL_SEPARATOR);
        TrieNode currentNode = root;
        for (String level : levels) {
            currentNode = currentNode.getChildren()
                    .computeIfAbsent(level, k -> new TrieNode());
        }
        currentNode.getSubscribers().add(clientId);
    }

    public void unsubscribeFrom(String topic, String clientId) {
        if (topic == null || topic.isEmpty()) {
            return;
        }
        validateTopic(topic);

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
        removeClientFromNode(root, clientId);
    }

    private void removeClientFromNode(TrieNode node, String clientId) {
        if (node == null || clientId == null) {
            return;
        }
        node.getSubscribers().remove(clientId);

        node.getChildren().values()
                .forEach(childNode -> removeClientFromNode(childNode, clientId));
    }

    public Set<String> getSubscribersFor(String topic) {
        if (topic == null || topic.isEmpty()) {
            return emptySet();
        }

        Set<String> matchingSubscribers = new CopyOnWriteArraySet<>();
        String[] levels = topic.split(TOPIC_LEVEL_SEPARATOR);

        SubscriptionVisitor visitor = new SubscriptionVisitor(levels, matchingSubscribers);
        root.accept(visitor);

        return unmodifiableSet(matchingSubscribers);
    }

    public void retainMessage(String topic, byte[] payload, MqttQoS qos) {
        if (topic == null || topic.isEmpty()) {
            return;
        }
        validateTopic(topic);

        String[] levels = topic.split(TOPIC_LEVEL_SEPARATOR);
        TrieNode currentNode = root;
        for (String level : levels) {
            currentNode = currentNode.getChildren()
                    .computeIfAbsent(level, k -> new TrieNode());
        }

        if (payload == null || payload.length == 0) {
            log.info("Clearing retained message for topic: {}", topic);
            currentNode.setRetainedMessage(null);
        } else {
            log.info("Retaining message for topic: {}, QoS: {}", topic, qos);
            currentNode.setRetainedMessage(new RetainedMessage(payload, qos));
        }
    }

    public List<RetainedMessageWithTopic> getRetainedMessagesMatching(String topic) {
        if (topic == null || topic.isEmpty()) {
            return emptyList();
        }

        log.debug("Searching for retained messages matching: {}", topic);

        List<RetainedMessageWithTopic> retainedMessages = new ArrayList<>();
        String[] levels = topic.split(TOPIC_LEVEL_SEPARATOR);

        RetainedMessageVisitor visitor = new RetainedMessageVisitor(levels, retainedMessages);
        root.accept(visitor);

        log.debug("Found {} retained messages for topic: {}", retainedMessages.size(), topic);
        return retainedMessages;
    }
}
