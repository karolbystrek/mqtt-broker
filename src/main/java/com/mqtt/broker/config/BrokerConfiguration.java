package com.mqtt.broker.config;

import lombok.Data;

@Data
public class BrokerConfiguration {
    private ServerProperties server = new ServerProperties();
    private MqttProperties mqtt = new MqttProperties();
    private LoggingProperties logging = new LoggingProperties();

    @Data
    public static class ServerProperties {
        private String host = "localhost";
        private int port = 1883;
    }

    @Data
    public static class MqttProperties {
        private long keepAliveCheckIntervalMs = 1000;
    }

    @Data
    public static class LoggingProperties {
        private String level = "INFO";
    }
}
