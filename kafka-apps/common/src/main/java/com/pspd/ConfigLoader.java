package com.pspd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// TODO: mover para configuração externa
public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RuntimeException("config.properties not found");
            }
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Error loading config.properties: " + e.getMessage());
        }
    }

    private static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Required property '" + key + "' not found or empty in config.properties");
        }

        return value;
    }

    public static final String BOOTSTRAP_SERVERS = getProperty("kafka.bootstrap.servers");
    public static final String GROUP_ID = getProperty("kafka.group.id");
    public static final String TOPIC = getProperty("kafka.topic");
    public static final String EXECUTABLE = getProperty("app.executable");
    public static final String MAX_POLLING_TIMEOUT = getProperty("kafka.max.poll.interval.ms");
}
