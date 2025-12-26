package com.mqtt.broker.auth.strategy;

import com.mqtt.broker.auth.AuthorizationEntry;
import com.mqtt.broker.auth.UserRegistry;
import com.mqtt.broker.trie.TopicTree;
import com.mqtt.broker.trie.strategy.AuthorizationInsertionStrategy;
import com.mqtt.broker.trie.strategy.AuthorizationLookupStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class FileBasedAuthorizationStrategy implements AuthorizationStrategy {

    private final UserRegistry userRegistry = new UserRegistry();
    private final TopicTree<Set<AuthorizationEntry>> authorizationTree = new TopicTree<>();

    public FileBasedAuthorizationStrategy() {
        populateAuthorizationTree();
    }

    private void populateAuthorizationTree() {
        for (var user : userRegistry.getAllUsers()) {
            if (user.permissions() != null) {
                for (var permission : user.permissions()) {
                    String[] levels = permission.topic().split("/");
                    var strategy = new AuthorizationInsertionStrategy(levels, new AuthorizationEntry(user.username(), permission.access()));
                    authorizationTree.perform(strategy);
                }
            }
        }
    }

    @Override
    public boolean authenticate(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        var user = userRegistry.getUserBy(username);
        return user != null && user.password().equals(password);
    }

    @Override
    public boolean canSubscribe(String username, String topicFilter) {
        var user = userRegistry.getUserBy(username);
        if (user == null) return false;
        if (user.permissions() == null || user.permissions().isEmpty()) return true;

        var entries = new CopyOnWriteArraySet<AuthorizationEntry>();
        String[] levels = topicFilter.split("/");
        var strategy = new AuthorizationLookupStrategy(levels, entries);
        authorizationTree.perform(strategy);

        return entries.stream()
                .anyMatch(e -> e.username().equals(username) && e.access().canRead());
    }

    @Override
    public boolean canPublish(String username, String topic) {
        var user = userRegistry.getUserBy(username);
        if (user == null) return false;
        if (user.permissions() == null || user.permissions().isEmpty()) return true;

        var entries = new CopyOnWriteArraySet<AuthorizationEntry>();
        String[] levels = topic.split("/");
        var strategy = new AuthorizationLookupStrategy(levels, entries);
        authorizationTree.perform(strategy);

        return entries.stream()
                .anyMatch(e -> e.username().equals(username) && e.access().canWrite());
    }
}
