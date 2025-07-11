package com.pspd;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final CombinedConfiguration appConfig;

    static {
        try {
            appConfig = (new Configurations()).combinedBuilder(new File("config.xml")).getConfiguration();
            appConfig.setThrowExceptionOnMissing(true);
            logger.info("Loaded config.");
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static Properties getConsumerProps() {
        Properties props = new Properties();

        appConfig.getKeys().forEachRemaining(key -> {
            if (key.startsWith("kafka")) props.put(key.substring("kafka.".length()), appConfig.getProperty(key));});

        return props;
    }

    public static Properties getProducerProps() {
        Properties props = getConsumerProps();
        props.remove("group.id");
        return props;
    }

    public static final String EXECUTABLE = appConfig.getString("app.executable");
    public static final String ENVIRONMENT = appConfig.getString("app.environment", "local");
    public static final String IMAGE = appConfig.getString("app.image");
    public static final String TOPIC = appConfig.getString("kafka.topic");
}
