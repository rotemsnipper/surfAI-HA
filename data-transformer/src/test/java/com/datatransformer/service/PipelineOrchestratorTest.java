package com.datatransformer.service;

import com.datatransformer.model.RawUser;
import com.datatransformer.model.TargetUser;
import com.datatransformer.pipeline.interfaces.Sink;
import com.datatransformer.pipeline.interfaces.Source;
import com.datatransformer.pipeline.interfaces.Transformer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineOrchestratorTest {

    @Mock
    private Source<RawUser, Path> source;

    @Mock
    private Transformer<RawUser, TargetUser> transformer;

    @Mock
    private Sink<TargetUser, Path> sink;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    private PipelineOrchestrator orchestrator;

    @BeforeEach
    void setUp() {

        when(meterRegistry.counter(anyString())).thenReturn(counter);

        orchestrator = new PipelineOrchestrator(source, transformer, sink, meterRegistry);

        ReflectionTestUtils.setField(orchestrator, "threadPoolSize", Runtime.getRuntime().availableProcessors());
        ReflectionTestUtils.setField(orchestrator, "queueCapacity", 100);
        ReflectionTestUtils.setField(orchestrator, "batchSize", 1000);
    }

    @Test
    void run_shouldProcessFiles_whenInputIsValid(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);
        Files.createFile(inputDir.resolve("test.json"));

        RawUser rawUser = mock(RawUser.class);
        TargetUser targetUser = mock(TargetUser.class);

        when(source.read(any(Path.class))).thenReturn(Stream.of(rawUser));
        when(transformer.transform(rawUser)).thenReturn(targetUser);
        doAnswer(invocation -> {
            Stream<?> stream = invocation.getArgument(0);
            stream.forEach(item -> {
            });
            return null;
        }).when(sink).write(any(), any(Path.class));

        // Act
        orchestrator.run(inputDir, outputDir);

        // Assert
        verify(source, times(1)).read(any(Path.class));
        verify(transformer, times(1)).transform(rawUser);
        verify(sink, times(1)).write(any(), any(Path.class));
    }

    @Test
    void run_shouldHandleProcessingError_whenSourceFails(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path inputDir = tempDir.resolve("input");
        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(inputDir);
        Files.createDirectories(outputDir);
        Files.createFile(inputDir.resolve("test.json"));

        when(source.read(any(Path.class))).thenThrow(new RuntimeException("Read error"));

        // Act
        orchestrator.run(inputDir, outputDir);

        // Assert
        verify(source, times(1)).read(any(Path.class));
        verify(sink, never()).write(any(), any());
    }
}
