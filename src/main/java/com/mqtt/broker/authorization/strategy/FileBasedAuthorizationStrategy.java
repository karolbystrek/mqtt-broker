package com.mqtt.broker.authorization.strategy;

import com.mqtt.broker.authorization.AuthorizationEntry;
import com.mqtt.broker.authorization.UserRegistry;
import com.mqtt.broker.packet.ConnectPacket;
import com.mqtt.broker.repository.AuthorizationRepository;
import com.mqtt.broker.trie.TopicPath;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class FileBasedAuthorizationStrategy implements AuthorizationStrategy {

    private final UserRegistry userRegistry = new UserRegistry();
    private final AuthorizationRepository authorizationRepository = new AuthorizationRepository();

    public FileBasedAuthorizationStrategy() {
        populateAuthorizationTree();
    }

    private void populateAuthorizationTree() {
        for (var user : userRegistry.getAllUsers()) {
            if (user.permissions() != null) {
                for (var permission : user.permissions()) {
                    authorizationRepository.add(permission.topic(), new AuthorizationEntry(user.username(), permission.access()));
                }
            }
        }
    }

    @Override
    public boolean authenticate(ConnectPacket packet) {
        if (!packet.variableHeader().hasUsername() || !packet.variableHeader().hasPassword()) {
            return false;
        }
        String username = packet.payload().username();
        String password = packet.payload().password();

        if (username == null || password == null) {
            return false;
        }
        var user = userRegistry.getUser(username);
        return user != null && user.password().equals(password);
    }

    @Override
    public boolean canSubscribe(String username, String topicFilter) {
        var user = userRegistry.getUser(username);
        if (user == null) return false;
        if (user.permissions() == null || user.permissions().isEmpty()) return true;

        var entries = new CopyOnWriteArraySet<AuthorizationEntry>();
        authorizationRepository.find(TopicPath.parse(topicFilter), entries);

        return entries.stream()
                .anyMatch(e -> e.username().equals(username) && e.access().canRead());
    }

    @Override
    public boolean canPublish(String username, String topic) {
        var user = userRegistry.getUser(username);
        if (user == null) return false;
        if (user.permissions() == null || user.permissions().isEmpty()) return true;

        var entries = new CopyOnWriteArraySet<AuthorizationEntry>();
        authorizationRepository.find(TopicPath.parse(topic), entries);

        return entries.stream()
                .anyMatch(e -> e.username().equals(username) && e.access().canWrite());
    }
}
