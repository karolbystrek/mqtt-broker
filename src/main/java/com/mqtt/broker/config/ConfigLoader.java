package com.mqtt.broker.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

@Slf4j
public class ConfigLoader {

    public static BrokerConfiguration load() {
        Yaml yaml = new Yaml(new Constructor(BrokerConfiguration.class, new org.yaml.snakeyaml.LoaderOptions()));
        BrokerConfiguration config = loadConfiguration(yaml);
        configureLogging(config);
        return config;
    }

    private static BrokerConfiguration loadConfiguration(Yaml yaml) {
        try (InputStream inputStream = java.nio.file.Files.newInputStream(java.nio.file.Paths.get("application.yml"))) {
            log.info("Loading configuration from external application.yml");
            return yaml.load(inputStream);
        } catch (Exception ignored) {
        }

        try (InputStream inputStream = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("application.yml")) {
            if (inputStream == null) {
                log.warn("Configuration file not found, using defaults.");
                return new BrokerConfiguration();
            }
            log.info("Loading configuration from internal resources");
            return yaml.load(inputStream);
        } catch (Exception e) {
            log.error("Failed to load configuration: {}", e.getMessage());
            log.info("Using default configuration.");
            return new BrokerConfiguration();
        }
    }

    private static void configureLogging(BrokerConfiguration config) {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.toLevel(config.getLogging().getLevel(), Level.INFO));
    }
}
