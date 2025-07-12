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

        try (KafkaProducer<String, GameOfLifeArgs> producer = new KafkaProducer<>(props);
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) break;

                processLine(input, producer);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while processing input", e);
        }

        System.out.println("Producer stopped");
    }

    private static void processLine(String input, KafkaProducer<String, GameOfLifeArgs> producer) {
        try {
            String[] nums = input.split(" ");
            if (nums.length < 2) throw new IllegalArgumentException("Expected two numbers");

            int min = Integer.parseInt(nums[0]);
            int max = Integer.parseInt(nums[1]);

            GameOfLifeArgs gameOfLifeArgs = new GameOfLifeArgs(min, max);
            System.out.println("Sending: " + gameOfLifeArgs);
            producer.send(new ProducerRecord<>(ConfigLoader.TOPIC, gameOfLifeArgs));
        } catch (NumberFormatException e) {
            logger.warn("Invalid number input: {}", input, e);
            System.err.println("Please enter two integers.");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input: {}", input, e);
            System.err.println("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", input, e);
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}
