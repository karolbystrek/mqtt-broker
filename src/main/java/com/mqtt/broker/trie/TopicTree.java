package com.mqtt.broker.trie;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.mqtt.broker.packet.MqttFixedHeader;
import com.mqtt.broker.packet.MqttQoS;
import com.mqtt.broker.packet.PublishPacket;

import static com.mqtt.broker.trie.TopicFilterValidator.validateTopicFilter;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Collections.emptyList;
import static com.mqtt.broker.packet.MqttControlPacketType.PUBLISH;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TopicTree {

    private final TrieNode root = new TrieNode();

    private static final String SINGLE_LEVEL_WILDCARD = "+";
    private static final String MULTI_LEVEL_WILDCARD = "#";
    private static final String TOPIC_LEVEL_SEPARATOR = "/";

    public void subscribeTo(String topic, String clientId) {
        if (topic == null || topic.isEmpty()) {
            return;
        }
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
        if (topic == null || topic.isEmpty()) {
            return;
        }
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
    public void retainMessage(String topic, byte[] payload, MqttQoS qos) {
        if (topic == null || topic.isEmpty()) {
            return;
        }
        validateTopicFilter(topic); 

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

    public List<PublishPacket> getRetainedMessagesMatching(String topicFilter) {
        if (topicFilter == null || topicFilter.isEmpty()) {
            return emptyList();
        }

        log.debug("Searching for retained messages matching: {}", topicFilter);

        List<PublishPacket> retainedPackets = new ArrayList<>();
        String[] levels = topicFilter.split(TOPIC_LEVEL_SEPARATOR);
        
        findRetainedMessages(root, levels, 0, retainedPackets, "");
        
        log.debug("Found {} retained messages for filter: {}", retainedPackets.size(), topicFilter);
        return retainedPackets;
    }

    private void findRetainedMessages(TrieNode node, String[] levels, int levelIndex, List<PublishPacket> retainedPackets, String currentPath) {
        if (levelIndex == levels.length) {
            addRetainedMessage(node, retainedPackets, currentPath);
            return;
        }

        String level = levels[levelIndex];

        if (MULTI_LEVEL_WILDCARD.equals(level)) {
            findAllRetainedMessages(node, retainedPackets, currentPath);
            return;
        }

        if (SINGLE_LEVEL_WILDCARD.equals(level)) {
            for (var entry : node.getChildren().entrySet()) {
                String childName = entry.getKey();
                if (levelIndex == 0 && childName.startsWith("$")) {
                    continue;
                }
                String nextPath = currentPath.isEmpty() ? childName : currentPath + "/" + childName;
                findRetainedMessages(entry.getValue(), levels, levelIndex + 1, retainedPackets, nextPath);
            }
            return;
        }

        TrieNode childNode = node.getChildren().get(level);
        if (childNode != null) {
            String nextPath = currentPath.isEmpty() ? level : currentPath + "/" + level;
            findRetainedMessages(childNode, levels, levelIndex + 1, retainedPackets, nextPath);
        }
    }

    private void findAllRetainedMessages(TrieNode node, List<PublishPacket> retainedPackets, String currentPath) {
        addRetainedMessage(node, retainedPackets, currentPath);
        
        for (var entry : node.getChildren().entrySet()) {
            String nextPath = currentPath.isEmpty() ? entry.getKey() : currentPath + "/" + entry.getKey();
            findAllRetainedMessages(entry.getValue(), retainedPackets, nextPath);
        }
    }

    private void addRetainedMessage(TrieNode node, List<PublishPacket> retainedPackets, String topicName) {
        RetainedMessage retained = node.getRetainedMessage();
        if (retained == null) {
            return;
        }

        log.info("Found retained message for topic: {}", topicName);

        byte flags = 1; // Retain = 1
        flags |= (retained.qos().getValue() << 1);
        
        var fixedHeader = new MqttFixedHeader(PUBLISH, flags, 0);
        
        int packetId = 1;
        if (retained.qos() != MqttQoS.AT_MOST_ONCE) {
            packetId = 1; // TODO: Use a proper Packet ID generator
        }
        
        var variableHeader = new PublishPacket.PublishVariableHeader(topicName, packetId);
        var packet = new PublishPacket(fixedHeader, variableHeader, retained.payload());
        retainedPackets.add(packet);
    }
}
