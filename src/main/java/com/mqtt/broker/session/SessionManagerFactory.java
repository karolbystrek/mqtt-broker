package com.mqtt.broker.session;

import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.session.persistence.strategy.FileSessionPersistenceStrategy;
import com.mqtt.broker.session.persistence.strategy.NoOpSessionPersistenceStrategy;
import com.mqtt.broker.session.persistence.strategy.SessionPersistenceStrategy;
import com.mqtt.broker.repository.SubscriptionRepository;

public class SessionManagerFactory {

    public static SessionManager create(BrokerConfiguration config, SubscriptionRepository subscriptionRepository) {
        SessionPersistenceStrategy persistenceStrategy;

        if (config.isCleanSession()) {
            persistenceStrategy = new NoOpSessionPersistenceStrategy();
        } else {
            persistenceStrategy = new FileSessionPersistenceStrategy();
        }

        return new SessionManager(subscriptionRepository, persistenceStrategy);
    }
}
