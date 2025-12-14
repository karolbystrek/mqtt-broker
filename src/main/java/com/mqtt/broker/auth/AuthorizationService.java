package com.mqtt.broker.auth;

import com.mqtt.broker.auth.strategy.AuthorizationStrategy;
import com.mqtt.broker.auth.strategy.FileBasedAuthorizationStrategy;
import com.mqtt.broker.auth.strategy.PermissiveAuthorizationStrategy;
import com.mqtt.broker.config.BrokerConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationService {

    private final AuthorizationStrategy strategy;

    public AuthorizationService(BrokerConfiguration config) {
        if (config.getServer().isAllowAnonymous()) {
            log.info("Anonymous access allowed.");
            this.strategy = new PermissiveAuthorizationStrategy();
        } else {
            log.info("Anonymous access disabled.");
            this.strategy = new FileBasedAuthorizationStrategy();
        }
    }

    public boolean authenticate(String username, String password) {
        return strategy.authenticate(username, password);
    }

    public boolean canSubscribe(String username, String topicFilter) {
        return strategy.canSubscribe(username, topicFilter);
    }

    public boolean canPublish(String username, String topic) {
        return strategy.canPublish(username, topic);
    }
}
