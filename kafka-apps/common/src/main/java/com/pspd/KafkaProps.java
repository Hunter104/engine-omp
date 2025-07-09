package com.pspd;

import java.util.Properties;

public class KafkaProps {
    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", ConfigLoader.BOOTSTRAP_SERVERS);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // TODO: propriedade de timeout de polling atualmente ignorada
        props.put("connections.max.idle.ms", "5000");
        props.put("request.timeout.ms", "10000");
        return props;
    }

    public static Properties getConsumerProps() {
        Properties props = getProperties();
        props.put("value.deserializer", "com.pspd.GsonDeserializer");
        props.put("gson.deserializer.type", GameOfLifeArgs.class.getName());
        props.put("group.id", ConfigLoader.GROUP_ID);
        return props;
    }

    public static Properties getProducerProps() {
        Properties props = getProperties();
        props.put("value.serializer", "com.pspd.GsonSerializer");
        return props;
    }
}
