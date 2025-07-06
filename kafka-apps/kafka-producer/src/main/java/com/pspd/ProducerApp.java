package com.pspd;

import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class ProducerApp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Gson gson = new Gson();
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        Message message = new Message(100, 500);
        producer.send(new ProducerRecord<>(Constants.TOPIC, gson.toJson(message)));
        producer.close();
    }
}