package com.pspd;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.IOException;

public class DistributedExecutor implements GameOfLifeExecutor {
    private final Logger logger = LoggerFactory.getLogger(DistributedExecutor.class);
    private final KubernetesClient kubernetesClient = new KubernetesClientBuilder().build();
    private final String jobTemplate;

    public DistributedExecutor() {
        logger.info("Starting distributed executor");
        logger.info("Loading job template...");
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("/job-template.yaml")) {
            assert is != null;
            jobTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            logger.info("Job template loaded successfully");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load job template", e);
        }
    }

    public void runGameOfLife(Message params) {
        String jobName = "game-of-life-" + UUID.randomUUID().toString().substring(0, 8);

        StringSubstitutor sub = new StringSubstitutor(createJobConfig(jobName, params.powMin(), params.powMax()));
        kubernetesClient.resource(sub.replace(jobTemplate)).create();
        logger.info("Created MPI job: {}", jobName);
    }

    private Map<String, String> createJobConfig(String jobName, int powMin, int powMax) {
        Map<String, String> config = new HashMap<>();

        config.put("job.name", jobName);
        config.put("job.namespace", ConfigLoader.NAMESPACE);
        config.put("job.image", ConfigLoader.IMAGE);
        config.put("job.executable", ConfigLoader.EXECUTABLE);
        config.put("job.powMin", String.valueOf(powMin));
        config.put("job.powMax", String.valueOf(powMax));

        // TODO: Definir número de workers dinamicamente
        config.put("mpi.slotsPerWorker", "1");
        config.put("mpi.workerReplicas", "4");

        return config;
    }
}
