package com.mqtt.broker.config;

import lombok.Data;

@Data
public class BrokerConfiguration {
    private ServerProperties server = new ServerProperties();
    private LoggingProperties logging = new LoggingProperties();

    @Data
    public static class ServerProperties {
        private String host = "localhost";
        private int port = 1883;
        private boolean cleanSession = true;
        private boolean allowAnonymous = true;
    }

    @Data
    public static class LoggingProperties {
        private String level = "INFO";
    }
}
