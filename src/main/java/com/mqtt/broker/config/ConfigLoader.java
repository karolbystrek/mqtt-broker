package com.mqtt.broker.config;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class ConfigLoader {

    private static final String CONFIG_FILE = "config.yml";

    public static BrokerConfiguration load() {
        var yaml = new Yaml(new Constructor(BrokerConfiguration.class, new LoaderOptions()));
        BrokerConfiguration config = loadConfiguration(yaml);
        log.info("Loaded configuration from {}", CONFIG_FILE);
        return config;
    }

    private static BrokerConfiguration loadConfiguration(Yaml yaml) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(CONFIG_FILE))) {
            return yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration from " + CONFIG_FILE, e);
        }
    }
}
