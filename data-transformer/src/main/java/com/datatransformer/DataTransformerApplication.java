package com.datatransformer;

import com.datatransformer.service.PipelineOrchestrator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

import java.nio.file.Paths;

@SpringBootApplication
@EnableRetry
public class DataTransformerApplication implements CommandLineRunner {

    private final PipelineOrchestrator orchestrator;

    public DataTransformerApplication(PipelineOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public static void main(String[] args) {
        SpringApplication.run(DataTransformerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String inputDir = null;
        String outputDir = null;

        for (String arg : args) {
            if (arg.startsWith("--input.dir=")) {
                inputDir = arg.substring("--input.dir=".length());
            } else if (arg.startsWith("--output.dir=")) {
                outputDir = arg.substring("--output.dir=".length());
            }
        }

        if (args.length == 0) {
            return;
        }

        orchestrator.run(Paths.get(inputDir), Paths.get(outputDir));
    }
}
