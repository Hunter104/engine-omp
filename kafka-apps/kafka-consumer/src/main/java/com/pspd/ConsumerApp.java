package com.pspd;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import static com.pspd.Constants.EXECUTABLE;

public class ConsumerApp {
    private static void runGameOfLife(Message message) throws IOException, InterruptedException {
        String command = String.format("mpirun -np 4 %s %d %d", EXECUTABLE, message.powMin(), message.powMax());
        System.out.println("Executing: " + command);
        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();
        CompletableFuture<Void> stdoutFuture = CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading stdout: " + e.getMessage());
            }
        });

        CompletableFuture<Void> stderrFuture = CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading stderr: " + e.getMessage());
            }
        });

        stdoutFuture.join();
        stderrFuture.join();
        if (exitCode != 0) {
            throw new RuntimeException("Process exited with code " + exitCode);
        }
    }
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", Constants.BOOTSTRAP_SERVERS);
        props.put("group.id", Constants.GROUP_ID);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Gson gson = new Gson();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(java.util.List.of(Constants.TOPIC));
        while (true) {
            consumer.poll(Duration.ofMillis(100)).forEach(record -> {
                Message message = gson.fromJson(record.value(), Message.class);
                System.out.println("Got: " + message + " executing...");
               try {
                   runGameOfLife(message);
               } catch (Exception e) {
                   System.err.println("Error executing game of life: " + e.getMessage());
               }
            });
        }
    }
}