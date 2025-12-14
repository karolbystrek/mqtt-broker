package com.mqtt.broker;

import com.mqtt.broker.config.ConfigLoader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        var config = ConfigLoader.load();
        var context = new BrokerContext(config);

        try (var broker = new Broker(config, context)) {
            Runtime.getRuntime().addShutdownHook(new Thread(broker::stop));
            broker.start();
        } catch (IOException e) {
            log.error("Failed to start broker: {}", e.getMessage());
        }
    }
}
