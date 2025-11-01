package com.mqtt.broker;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try (var broker = new Broker()) {
            broker.start();
        } catch (IOException e) {
            System.err.println("Failed to start broker: " + e.getMessage());
        }
    }
}
