package com.mqtt.broker;

import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.config.ConfigLoader;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.service.PendingMessageDeliveryService;
import com.mqtt.broker.trie.TopicTree;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        BrokerConfiguration config = ConfigLoader.load();
        
        MqttPacketEncoder encoder = new MqttPacketEncoder();
        TopicTree topicTree = new TopicTree();
        PendingMessageDeliveryService pendingMessageService = new PendingMessageDeliveryService(encoder);
        
        BrokerContext context = new BrokerContext(config, topicTree, pendingMessageService);
        
        try (Broker broker = new Broker(config, context)) {
            broker.start();
        } catch (IOException e) {
            log.error("Failed to start broker: {}", e.getMessage());
        }
    }
}
