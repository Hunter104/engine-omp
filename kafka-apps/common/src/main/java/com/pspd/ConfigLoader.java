package com.pspd;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final CombinedConfiguration appConfig;
    private static final PropertiesConfiguration kafkaConfig;
    private static final String MAIN_CONFIG_FILE = "app.properties";
    private static final String KAFKA_CONFIG_FILE = "kafka.properties";

    static {
        try {
            appConfig = new CombinedConfiguration(new OverrideCombiner());
            Configurations configs = new Configurations();
            appConfig.setThrowExceptionOnMissing(true);

            kafkaConfig = configs.properties(ConfigLoader.class.getClassLoader().getResource(KAFKA_CONFIG_FILE));
            kafkaConfig.setThrowExceptionOnMissing(true);
            logger.info("Loaded kafka config from classpath {}", kafkaConfig);


            File externalFile = new File(MAIN_CONFIG_FILE);
            if (externalFile.exists()) {
                appConfig.addConfiguration(configs.properties(externalFile));
                logger.info("Loaded external config from {}", externalFile.getAbsolutePath());
            } else {
                logger.warn("External config file not found: {}", externalFile.getAbsolutePath());
            }

            PropertiesConfiguration defaultConfig = configs.properties(
                    ConfigLoader.class.getClassLoader().getResource(MAIN_CONFIG_FILE)
            );
            logger.info("Loaded default config from classpath");
            appConfig.addConfiguration(defaultConfig);

        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static final String EXECUTABLE = appConfig.getString("app.executable");
    public static final String ENVIRONMENT = appConfig.getString("app.environment", "local");
    public static final String IMAGE = appConfig.getString("app.image");
    public static final String TOPIC = appConfig.getString("kafka.topic");

    private static List<Properties> getWithPrefix(Configuration config, String prefix) {
        throw new NotImplementedException();
    }
    public static Properties getConsumerProps() {
        Properties props = new Properties();

        appConfig.getKeys().forEachRemaining(key -> {
            if (key.startsWith("kafka.")) {
                props.put(key.substring(6), appConfig.getString(key));
            }
        });

        kafkaConfig.getKeys().forEachRemaining(key -> {
            if (key.startsWith("kafka.")) {
                props.put(key.substring(6), kafkaConfig.getString(key));
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
