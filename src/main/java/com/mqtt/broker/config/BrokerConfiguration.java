package com.mqtt.broker.config;

import lombok.Data;

@Data
public class BrokerConfiguration {
    private String host = "localhost";
    private int port = 1883;
    private boolean cleanSession = true;
    private boolean allowAnonymous = true;
}