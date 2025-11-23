package com.datatransformer.service;

import com.datatransformer.pipeline.interfaces.Sink;
import com.datatransformer.pipeline.interfaces.Source;
import com.datatransformer.pipeline.interfaces.Transformer;
import com.datatransformer.model.RawUser;
import com.datatransformer.model.TargetUser;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class PipelineOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(PipelineOrchestrator.class);

    private final Source<RawUser, Path> source;
    private final Transformer<RawUser, TargetUser> transformer;
    private final Sink<TargetUser, Path> sink;
    private final Counter successCounter;
    private final Counter failureCounter;

    @Value("${pipeline.thread-pool.size:#{T(java.lang.Runtime).getRuntime().availableProcessors()}}")
    private int threadPoolSize;

    @Value("${pipeline.queue.capacity:100}")
    private int queueCapacity;

    @Value("${pipeline.batch.size:50000}")
    private int batchSize;

    public PipelineOrchestrator(Source<RawUser, Path> source,
            Transformer<RawUser, TargetUser> transformer,
            Sink<TargetUser, Path> sink,
            MeterRegistry meterRegistry) {
        this.source = source;
        this.transformer = transformer;
        this.sink = sink;
        this.successCounter = meterRegistry.counter("files.processed.success");
        this.failureCounter = meterRegistry.counter("files.processed.failure");
    }

    public void run(Path inputDir, Path outputDir) {
        logger.info("Starting pipeline. Input: {}, Output: {}", inputDir, outputDir);

        // Create processed and failed directories
        Path processedDir = inputDir.resolve("processed");
        Path failedDir = inputDir.resolve("failed");
        try {
            Files.createDirectories(processedDir);
            Files.createDirectories(failedDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create lifecycle directories", e);
        }

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                threadPoolSize,
                threadPoolSize,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        try {
            try (Stream<Path> files = Files.list(inputDir)) {
                files.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(file -> {
                            executor.submit(() -> {
                                try {
                                    processFileWithRetry(file, outputDir, processedDir, failedDir);
                                } catch (Exception e) {
                                    logger.error("Unexpected error in executor task for file: {}", file, e);
                                }
                            });
                        });
            }

            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
        logger.info("Pipeline completed.");
    }

    @Retryable(retryFor = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void processFileWithRetry(Path inputFile, Path outputDir, Path processedDir, Path failedDir)
            throws Exception {
        String filename = inputFile.getFileName().toString();
        MDC.put("filename", filename);
        try {
            logger.info("Processing file: {}", inputFile);
            processFile(inputFile, outputDir);

            // Move to processed directory
            Path targetPath = processedDir.resolve(filename);
            Files.move(inputFile, targetPath, StandardCopyOption.REPLACE_EXISTING);

            successCounter.increment();
            logger.info("Successfully processed and moved file: {}", inputFile);
        } catch (Exception e) {
            logger.error("Error processing file: {}", inputFile, e);
            throw e; // Re-throw to trigger retry
        } finally {
            MDC.remove("filename");
        }
    }

    @Recover
    public void recoverFromProcessingFailure(Exception e, Path inputFile, Path outputDir, Path processedDir,
            Path failedDir) {
        String filename = inputFile.getFileName().toString();
        MDC.put("filename", filename);
        try {
            logger.error("Failed to process file after retries: {}", inputFile, e);

            // Move to failed directory
            Path targetPath = failedDir.resolve(filename);
            Files.move(inputFile, targetPath, StandardCopyOption.REPLACE_EXISTING);

            failureCounter.increment();
            logger.info("Moved failed file to: {}", targetPath);
        } catch (IOException ioException) {
            logger.error("Failed to move file to failed directory: {}", inputFile, ioException);
        } finally {
            MDC.remove("filename");
        }
    }

    private void processFile(Path inputFile, Path outputDir) {
        String filename = inputFile.getFileName().toString();
        Path outputFile = outputDir.resolve(filename);

        try (Stream<RawUser> rawUsers = source.read(inputFile)) {
            // Process in batches
            List<RawUser> batch = new ArrayList<>(batchSize);
            List<TargetUser> transformedBatch = new ArrayList<>(batchSize);

            rawUsers.forEach(rawUser -> {
                batch.add(rawUser);
                if (batch.size() >= batchSize) {
                    // Transform batch
                    for (RawUser user : batch) {
                        transformedBatch.add(transformer.transform(user));
                    }
                    batch.clear();
                }
            });

            // Process remaining records
            if (!batch.isEmpty()) {
                for (RawUser user : batch) {
                    transformedBatch.add(transformer.transform(user));
                }
            }

            // Write all transformed records
            sink.write(transformedBatch.stream(), outputFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
