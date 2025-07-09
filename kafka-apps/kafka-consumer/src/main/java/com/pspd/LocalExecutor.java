package com.pspd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

import static com.pspd.ConfigLoader.EXECUTABLE;

// TODO: WIP, adicionar mpi funfando com kubernetes
public class LocalExecutor implements  GameOfLifeExecutor {
    private final Logger logger = LoggerFactory.getLogger(LocalExecutor.class);

    public LocalExecutor() {
        int exitCode;
        logger.info("Local GOL Executor starting...");
        try {
            logger.info("Checking executable: {}", EXECUTABLE);
            Process process = Runtime.getRuntime().exec("which " + ConfigLoader.EXECUTABLE);
            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error checking executable: " + e.getMessage());
        }
        if (exitCode != 0)
            throw new RuntimeException("Executable not found: " + ConfigLoader.EXECUTABLE);
        logger.info("Executable found: {}", EXECUTABLE);
        logger.info("Local GOL Executor checking complete");
    }

    public void runGameOfLife(Message params) {
        int exitCode;
        try {
            String command = String.format("mpirun -np 1 %s %d %d", EXECUTABLE, params.powMin(), params.powMax());
            Process process = Runtime
                    .getRuntime()
                    .exec(command);
            logger.info("Executing: {}", command);
            readOutputStream(process.getInputStream()).thenAccept(output -> {
                if (!output.isBlank()) {
                    logger.info(output);
                }
            });
            readOutputStream(process.getErrorStream()).thenAccept(error -> {
                if (!error.isBlank()) {
                    logger.error(error);
                }
            });
            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error executing game of life: " + e.getMessage());
        }
        if (exitCode != 0) throw new RuntimeException("Process exited with code " + exitCode);
    }

    private CompletableFuture<String> readOutputStream(InputStream inputStream) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            reader.lines().forEach(l -> builder.append(l).append("\n"));
        } catch (IOException e) {
            logger.error("Error reading output stream: {}", e.getMessage());
        }
        return CompletableFuture.completedFuture(builder.toString());
    }
}
