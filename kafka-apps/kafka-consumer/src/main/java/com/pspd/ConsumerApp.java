package com.pspd;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Properties;

public class ConsumerApp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", Constants.BOOTSTRAP_SERVERS);
        props.put("group.id", "gol-consumer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Gson gson = new Gson();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(java.util.List.of(Constants.TOPIC));
        while (true) {
            consumer.poll(Duration.ofMillis(100)).forEach(record -> {
                Message message = gson.fromJson(record.value(), Message.class);
                System.out.println("Got: " + message);
            });
        }
    }
}