package com.mqtt.broker;

import com.mqtt.broker.config.ConfigLoader;
import com.mqtt.broker.event.BrokerEventPublisher;
import com.mqtt.broker.event.listener.ConnectionEventListener;
import com.mqtt.broker.event.listener.DeliveryEventListener;
import com.mqtt.broker.event.listener.SubscriptionEventListener;
import com.mqtt.broker.interceptor.ClientActivityInterceptor;
import com.mqtt.broker.interceptor.EventPublishingInterceptor;
import com.mqtt.broker.interceptor.PacketAuthorizationInterceptor;
import com.mqtt.broker.interceptor.PacketHandlingInterceptor;
import com.mqtt.broker.interceptor.PacketProcessingPipeline;
import com.mqtt.broker.interceptor.ResponseSendingInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        var config = ConfigLoader.load();
        var context = new BrokerContext(config);

        var eventPublisher = BrokerEventPublisher.builder()
                .addListener(new ConnectionEventListener(context))
                .addListener(new SubscriptionEventListener(context))
                .addListener(new DeliveryEventListener(context))
                .build();

        var pipeline = PacketProcessingPipeline.builder()
                .addInterceptor(new EventPublishingInterceptor(eventPublisher))
                .addInterceptor(new ResponseSendingInterceptor(context))
                .addInterceptor(new ClientActivityInterceptor(context))
                .addInterceptor(new PacketAuthorizationInterceptor(context))
                .addInterceptor(new PacketHandlingInterceptor(context))
                .build();

        try (var broker = Broker.builder()
                .config(config)
                .context(context)
                .eventPublisher(eventPublisher)
                .pipeline(pipeline)
                .build()
        ) {

            Runtime.getRuntime().addShutdownHook(new Thread(broker::stop));
            broker.start();
        } catch (IOException e) {
            log.error("Failed to start broker: {}", e.getMessage());
        }
    }
}
