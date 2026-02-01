package com.mqtt.broker.config;

import lombok.Data;

@Data
public class BrokerConfiguration {
    private String host = "localhost";
    private int port = 1883;
    private boolean cleanSession = true;
    private boolean allowAnonymous = true;
    private String usersFile;
    private String authStrategy;
    private DatabaseConfiguration database;

    @Data
    public static class DatabaseConfiguration {
        private String url;
        private String username;
        private String password;
        private String driver;
    }
}
