package com.pspd;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final CompositeConfiguration configs = new CompositeConfiguration();
    private static final Properties kafkaClientProps = new Properties();

    static {
        Configurations helper = new Configurations();
        configs.setThrowExceptionOnMissing(true);
        try {
            kafkaClientProps.load(ConfigLoader.class.getResourceAsStream("/kafka.properties"));
            configs.addConfiguration(new SystemConfiguration());
            configs.addConfiguration(new EnvironmentConfiguration());
            configs.addConfiguration(helper.properties(new File("config.properties")));

            logger.info("Configuration loaded succesfully");
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String EXECUTABLE = configs.getString("app.executable");
    public static final String ENVIRONMENT = configs.getString("app.environment", "local");
    public static final String IMAGE = configs.getString("app.image");
    public static Properties getConsumerProps() {
        Properties props = new Properties();
        props.putAll(kafkaClientProps);
        return props;
    }
    public static Properties getProducerProps() {
        Properties props = new Properties();
        props.putAll(kafkaClientProps);
        props.remove("group.id");
        return props;
    }
}
