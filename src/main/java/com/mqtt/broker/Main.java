package com.mqtt.broker;

import com.mqtt.broker.config.BrokerConfiguration;
import com.mqtt.broker.config.ConfigLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        BrokerConfiguration config = ConfigLoader.load();
        try (Broker broker = new Broker(config)) {
            broker.start();
        } catch (IOException e) {
            log.error("Failed to start broker: {}", e.getMessage());
        }
    }
}
