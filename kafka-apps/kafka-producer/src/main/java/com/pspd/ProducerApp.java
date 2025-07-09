package com.pspd;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Scanner;

public class ProducerApp {
    public static void main(String[] args) {
        Properties props = KafkaProps.getProducerProps();
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
            System.err.println("Invalid input");
        } catch (IllegalArgumentException e) {
            System.err.println("Error while creating message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
        System.out.println("Producer stopped");
    }
}