package com.mqtt.broker.authorization;

import com.mqtt.broker.authorization.strategy.AuthorizationStrategy;
import com.mqtt.broker.authorization.strategy.FileBasedAuthorizationStrategy;
import com.mqtt.broker.authorization.strategy.PermissiveAuthorizationStrategy;
import com.mqtt.broker.config.BrokerConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationServiceFactory {

    public static AuthorizationService create(BrokerConfiguration config) {
        AuthorizationStrategy strategy;
        if (config.isAllowAnonymous()) {
            log.info("Anonymous access allowed.");
            strategy = new PermissiveAuthorizationStrategy();
        } else {
            log.info("Anonymous access disabled.");
            strategy = new FileBasedAuthorizationStrategy();
        }
        return new AuthorizationService(strategy);
    }
}
