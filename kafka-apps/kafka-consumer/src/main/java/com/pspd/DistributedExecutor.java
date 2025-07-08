package com.pspd;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.io.IOException;

public class DistributedExecutor implements GameOfLifeExecutor {
    private final Logger logger = LoggerFactory.getLogger(DistributedExecutor.class);
    private final KubernetesClient kubernetesClient;
    private final String jobTemplate;

    public DistributedExecutor() {
        kubernetesClient = new KubernetesClientBuilder().build();
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("/job-template.yaml")) {
            assert is != null;
            jobTemplate = Arrays.toString(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load job template", e);
        }
    }

    public void runGameOfLife(Message params) {
        String jobName = "game-of-life-" + UUID.randomUUID().toString().substring(0, 8);

        try {
            StringSubstitutor sub = new StringSubstitutor(createJobConfig(jobName, params.powMin(), params.powMax()));
            kubernetesClient.resource(sub.replace(jobTemplate)).create();
            logger.info("Created MPI job: {}", jobName);
        } catch (Exception e) {
            logger.error("Failed to create MPI job: {}", e.getMessage());
            throw new RuntimeException("Failed to execute Game of Life", e);
        }
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

        config.put("resources.requests.memory", "512Mi");
        config.put("resources.requests.cpu", "500m");
        config.put("resources.limits.memory", "1Gi");
        config.put("resources.limits.cpu", "1000m");

        return config;
    }
}
