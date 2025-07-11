package com.pspd;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final CombinedConfiguration combinedConfig;
    private static final PropertiesConfiguration kafkaConfig;

    static {
        try {
            combinedConfig = new CombinedConfiguration(new OverrideCombiner());
            Configurations configs = new Configurations();
            combinedConfig.setThrowExceptionOnMissing(true);

            kafkaConfig = configs.properties(
                    ConfigLoader.class.getClassLoader().getResource("kafka.properties")
            );
            logger.info("Loaded kafka config from classpath");


            File externalFile = new File("app.properties");
            if (externalFile.exists()) {
                combinedConfig.addConfiguration(configs.properties(externalFile));
                logger.info("Loaded external config from {}", externalFile.getAbsolutePath());
            } else {
                logger.warn("External config file not found: {}", externalFile.getAbsolutePath());
            }

            PropertiesConfiguration defaultConfig = configs.properties(
                    ConfigLoader.class.getClassLoader().getResource("app.properties")
            );
            logger.info("Loaded default config from classpath");
            combinedConfig.addConfiguration(defaultConfig);

        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static final String EXECUTABLE = combinedConfig.getString("app.executable");
    public static final String ENVIRONMENT = combinedConfig.getString("app.environment", "local");
    public static final String IMAGE = combinedConfig.getString("app.image");
    public static final String TOPIC = combinedConfig.getString("kafka.topic");

    public static Properties getConsumerProps() {
        Properties props = new Properties();

        combinedConfig.getKeys().forEachRemaining(key -> {
            if (key.startsWith("kafka.")) {
                props.put(key.substring(6), combinedConfig.getString(key));
            }
        });

        kafkaConfig.getKeys().forEachRemaining(key -> {
            if (key.startsWith("kafka.")) {
                props.put(key.substring(6), combinedConfig.getString(key));
            }
        });

        return props;
    }

    public static Properties getProducerProps() {
        Properties props = getConsumerProps();
        props.remove("group.id");
        return props;
    }
}
