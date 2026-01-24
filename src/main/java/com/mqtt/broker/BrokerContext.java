package com.mqtt.broker;

import com.mqtt.broker.authorization.AuthorizationService;
import com.mqtt.broker.authorization.AuthorizationServiceFactory;
import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.repository.RetainedMessageRepository;
import com.mqtt.broker.repository.SubscriptionRepository;
import com.mqtt.broker.service.MessageDeliveryService;
import com.mqtt.broker.session.SessionManager;
import com.mqtt.broker.session.SessionManagerFactory;
import lombok.Getter;

@Getter
public class BrokerContext {

    private final SubscriptionRepository subscriptionRepository = new SubscriptionRepository();
    private final RetainedMessageRepository retainedMessageRepository = new RetainedMessageRepository();

    private final AuthorizationService authorizationService;
    private final MessageDeliveryService messageDeliveryService;
    private final SessionManager sessionManager;

    public BrokerContext(BrokerConfiguration config) {
        this.authorizationService = AuthorizationServiceFactory.create(config);
        this.sessionManager = SessionManagerFactory.create(config, subscriptionRepository);
        this.messageDeliveryService = new MessageDeliveryService(sessionManager, subscriptionRepository, retainedMessageRepository);
    }
}
