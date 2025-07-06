package com.pspd;

import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Scanner;

public class ProducerApp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Gson gson = new Gson();
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Producer started, enter min and max values separated by space (quit with exit):");
        try {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                String[] nums = input.split(" ");
                Message message = new Message(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
                String json = gson.toJson(message);
                System.out.println("Sending: " + json);
                producer.send(new ProducerRecord<>(Constants.TOPIC, json));
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid input");
        } catch (IllegalArgumentException e) {
            System.err.println("Error while creating message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        } finally {
            scanner.close();
            producer.close();
        }
        System.out.println("Producer stopped");
    }
}