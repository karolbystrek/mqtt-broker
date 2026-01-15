package com.mqtt.broker.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class ConfigLoader {

    public static BrokerConfiguration load() {
        Yaml yaml = new Yaml(new Constructor(BrokerConfiguration.class, new org.yaml.snakeyaml.LoaderOptions()));
        BrokerConfiguration config = loadConfiguration(yaml);
        configureLogging();
        return config;
    }

    private static BrokerConfiguration loadConfiguration(Yaml yaml) {
        try (InputStream inputStream = Files.newInputStream(Paths.get("config.yml"))) {
            log.info("Loading configuration from config.yml");
            return yaml.load(inputStream);
        } catch (Exception e) {
            log.warn("Configuration file config.yml not found in root directory. Using defaults.");
            return new BrokerConfiguration();
        }
    }

    private static void configureLogging() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }
}
