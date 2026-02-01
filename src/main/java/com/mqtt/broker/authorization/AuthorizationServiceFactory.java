package com.mqtt.broker.authorization;

import com.mqtt.broker.authorization.strategy.AuthorizationStrategy;
import com.mqtt.broker.authorization.strategy.DatabaseAuthorizationStrategy;
import com.mqtt.broker.authorization.strategy.FileBasedAuthorizationStrategy;
import com.mqtt.broker.authorization.strategy.PermissiveAuthorizationStrategy;
import com.mqtt.broker.config.BrokerConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationServiceFactory {

    public static AuthorizationService create(BrokerConfiguration config) {
        AuthorizationStrategy strategy;
        String authStrategyInfo = config.getAuthStrategy();

        switch (authStrategyInfo.toLowerCase()) {
            case "database":
                log.info("Using Database Authorization Strategy.");
                if (config.getDatabase() == null) {
                    throw new IllegalArgumentException("Database configuration missing.");
                }
                strategy = new DatabaseAuthorizationStrategy(config.getDatabase());
                break;
            case "file":
                log.info("Using File-based Authorization Strategy.");
                strategy = new FileBasedAuthorizationStrategy(config.getUsersFile());
                break;
            case "anonymous":
                log.info("Using Permissive Authorization Strategy.");
                strategy = new PermissiveAuthorizationStrategy();
                break;
            default:
                log.warn("Unknown auth strategy '{}', falling back to legacy determination.", authStrategyInfo);
                throw new IllegalArgumentException("Unknown auth strategy '" + authStrategyInfo + "'");
        }

        return new AuthorizationService(strategy);
    }
}
