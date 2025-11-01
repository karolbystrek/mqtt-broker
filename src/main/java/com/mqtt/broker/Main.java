package com.mqtt.broker;

public class Main {

    public static void main(String[] args) {
        try (var broker = new Broker()) {
            broker.start();
        } catch (Exception e) {
            System.err.println("Failed to start broker: " + e.getMessage());
        }
    }
}
