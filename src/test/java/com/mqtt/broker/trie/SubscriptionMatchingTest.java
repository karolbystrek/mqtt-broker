package com.mqtt.broker.trie;

import com.mqtt.broker.trie.visitor.SubscriptionAddVisitor;
import com.mqtt.broker.trie.visitor.SubscriptionFinderVisitor;
import java.util.HashSet;
import java.util.Set;

public class SubscriptionMatchingTest {

    public static void main(String[] args) {
        testMatch("sport/tennis/player1", "sport/tennis/#", true);
        testMatch("sport/tennis", "sport/tennis/#", true); // Parent match
        testMatch("sport/tennis", "sport/+", true);
        testMatch("sport", "sport/+", false); // Level mismatch
        testMatch("sport/tennis/player1", "sport/+", false);
        testMatch("sport/tennis/player1", "#", true);
        testMatch("sport", "+", true);
        testMatch("sport/tennis", "+", false);
        testMatch("sport/tennis", "+/tennis", true);
        testMatch("sport/tennis/player1", "sport/+/player1", true);

        // System topics
        testMatch("$SYS/monitor/Clients", "#", false); // Should not match
        testMatch("$SYS/monitor/Clients", "+/monitor/Clients", false); // Should not match
        testMatch("$SYS/monitor/Clients", "$SYS/monitor/Clients", true);
        testMatch("$SYS/monitor/Clients", "$SYS/#", true);
        
        System.out.println("All tests finished.");
    }

    private static void testMatch(String topic, String subscription, boolean expected) {
        TopicTree<Set<String>> tree = new TopicTree<>();
        String clientId = "client1";
        
        // Add subscription
        String[] subLevels = subscription.split("/");
        // Handle edge case where split might behave differently for "sport/" etc if we tested that
        // But for these tests simple split is fine.
        // Special case: if subscription is just "#" or "+", split works fine.
        
        SubscriptionAddVisitor addVisitor = new SubscriptionAddVisitor(subLevels, clientId);
        tree.accept(addVisitor);

        // Find matches
        String[] topicLevels = topic.split("/");
        Set<String> matches = new HashSet<>();
        SubscriptionFinderVisitor findVisitor = new SubscriptionFinderVisitor(topicLevels, matches);
        tree.accept(findVisitor);

        boolean actual = matches.contains(clientId);
        if (actual != expected) {
            System.err.println("FAIL: Topic '" + topic + "' Sub '" + subscription + 
                "' Expected: " + expected + " Actual: " + actual);
        } else {
            System.out.println("PASS: Topic '" + topic + "' Sub '" + subscription + "'");
        }
    }
}
