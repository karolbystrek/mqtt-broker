package com.mqtt.broker;

import com.mqtt.broker.auth.UserRegistry;
import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.config.ConfigLoader;
import com.mqtt.broker.context.BrokerContext;
import com.mqtt.broker.trie.TopicTree;
import lombok.extern.slf4j.Slf4j;

import com.mqtt.broker.encoder.MqttPacketEncoder;
import com.mqtt.broker.service.MqttPacketSender;
import com.mqtt.broker.service.PendingMessageDeliveryService;

import java.io.IOException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        BrokerConfiguration config = ConfigLoader.load();
        
        TopicTree topicTree = new TopicTree();
        UserRegistry userRegistry = new UserRegistry("passwd");
        
        MqttPacketEncoder encoder = new MqttPacketEncoder();
        MqttPacketSender packetSender = new MqttPacketSender(encoder);
        PendingMessageDeliveryService pendingMessageService = new PendingMessageDeliveryService(packetSender);
        
        BrokerContext context = new BrokerContext(config, topicTree, userRegistry, packetSender, pendingMessageService );
        
        
        try (Broker broker = new Broker(config, context)) {
            broker.start();
        } catch (IOException e) {
            log.error("Failed to start broker: {}", e.getMessage());
        }
    }
}
