package com.pspd;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ConsumerApp {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerApp.class);
    private static final BlockingQueue<ConsumerRecord<String, GameOfLifeArgs>> queue = new LinkedBlockingQueue<>(1000);
    private static final GameOfLifeExecutor executor;

    static {
        if (ConfigLoader.ENVIRONMENT.equalsIgnoreCase("local") ){
            logger.info("Running in local mode");
            executor = new LocalExecutor();
        } else {
            logger.info("Running in distributed mode");
            executor = new DistributedExecutor();
        }
    }

    private static void consumeRecord(ConsumerRecord<String, GameOfLifeArgs> record) {
        try {
            queue.put(record);
            logger.info("Enqueued record: {}", record.value());
        } catch (InterruptedException e) {
            logger.warn("Interrupted while waiting to enqueue record", e);
            Thread.currentThread().interrupt();
        }
    }

    private static void recordProcessingThread() {
        while (true) {
            try {
                ConsumerRecord<String, GameOfLifeArgs> record = queue.take();
                logger.info("Processing: {}", record.value());
                executor.runGameOfLife(record.value());
            } catch (InterruptedException e) {
                logger.info("Processing interrupted, exiting");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("Error processing record: {}", e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        logger.info("Loading properties...");
        Properties props = ConfigLoader.getConsumerProps();
        logger.info("Starting consumer...");
        KafkaConsumer<String, GameOfLifeArgs> consumer = new KafkaConsumer<>(props);
        logger.info("Subscribing to topic...");
        consumer.subscribe(java.util.List.of(ConfigLoader.TOPIC));

        Thread processorThread = new Thread(ConsumerApp::recordProcessingThread);
        processorThread.start();

        logger.info("Polling for messages...");
        while (true) consumer.poll(Duration.ofMillis(100)).forEach(ConsumerApp::consumeRecord);
    }

}