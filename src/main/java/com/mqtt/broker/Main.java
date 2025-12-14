package com.mqtt.broker;

import com.mqtt.broker.config.ConfigLoader;
import com.mqtt.broker.pipeline.PipelineFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Main {

    public static void main(String[] args) {
        var config = ConfigLoader.load();
        var context = new BrokerContext(config);
        var pipeline = PipelineFactory.create(context);

        try (var broker = new Broker(config, context, pipeline)) {
            Runtime.getRuntime().addShutdownHook(new Thread(broker::stop));
            broker.start();
        } catch (IOException e) {
            log.error("Failed to start broker: {}", e.getMessage());
        }
    }
}
