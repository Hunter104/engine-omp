package com.pspd;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final CompositeConfiguration configs = new CompositeConfiguration();

    static {
        Configurations helper = new Configurations();
        configs.setThrowExceptionOnMissing(true);
        try {
            configs.addConfiguration(new SystemConfiguration());
            configs.addConfiguration(new EnvironmentConfiguration());
            configs.addConfiguration(helper.properties(new File("config.properties")));

            logger.info("Configuration loaded succesfully");
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String BOOTSTRAP_SERVERS = configs.getString("kafka.bootstrap.servers");
    public static final String GROUP_ID = configs.getString("kafka.group.id");
    public static final String TOPIC = configs.getString("kafka.topic");
    public static final String EXECUTABLE = configs.getString("app.executable");
    public static final String ENVIRONMENT = configs.getString("app.environment", "local");
    public static final String IMAGE = configs.getString("app.image");
    public static final String NAMESPACE = configs.getString("app.namespace");
}
