package com.datatransformer.component;

import com.datatransformer.model.TargetUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonFileSinkTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonFileSink sink = new JsonFileSink(objectMapper);

    @Test
    void write_shouldWriteUsersToFile_whenStreamIsValid(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path outputFile = tempDir.resolve("output.json");
        TargetUser user = new TargetUser(
                "1", "ext-1", "mail@example.com", "Member", "US", true, "First", "Last",
                "2023-01-01", "2023-01-01");
        Stream<TargetUser> userStream = Stream.of(user);

        // Act
        sink.write(userStream, outputFile);

        // Assert
        assertThat(outputFile).exists();
        String content = Files.readString(outputFile);
        assertThat(content).contains("ext-1");
        assertThat(content).contains("mail@example.com");
    }

    @Test
    void write_shouldThrowException_whenOutputIsInvalid(@TempDir Path tempDir) {
        // Arrange
        Path invalidPath = tempDir.resolve("non_existent_dir").resolve("output.json");
        TargetUser user = new TargetUser(
                "1", "ext-1", "mail@example.com", "Member", "US", true, "First", "Last", null, null);
        Stream<TargetUser> userStream = Stream.of(user);

        // Act & Assert
        assertThatThrownBy(() -> sink.write(userStream, invalidPath))
                .isInstanceOf(UncheckedIOException.class);
    }
}
