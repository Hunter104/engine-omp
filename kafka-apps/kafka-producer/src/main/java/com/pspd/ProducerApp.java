package com.pspd;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Scanner;

public class ProducerApp {
    private static final Logger logger = LoggerFactory.getLogger(ProducerApp.class);

    public static void main(String[] args) {
        Properties props = ConfigLoader.getProducerProps();
        System.out.println("Producer started, enter min and max values separated by a space (quit with exit):");
        try (KafkaProducer<String, GameOfLifeArgs> producer = new KafkaProducer<>(props); Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                String[] nums = input.split(" ");
                GameOfLifeArgs gameOfLifeArgs = new GameOfLifeArgs(Integer.parseInt(nums[0]), Integer.parseInt(nums[1]));
                System.out.println("Sending: " + gameOfLifeArgs);
                producer.send(new ProducerRecord<>(ConfigLoader.TOPIC, gameOfLifeArgs));
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid input: {}", e.getMessage());
            System.err.println("Invalid input");
        } catch (IllegalArgumentException e) {
            logger.error("Error creating message {}", e.getMessage());
            System.err.println("Error while creating message: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage());
            System.err.println("Error sending message: " + e.getMessage());
        }
        System.out.println("Producer stopped");
    }
}