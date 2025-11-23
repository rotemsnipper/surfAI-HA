package com.datatransformer.component;

import com.datatransformer.model.RawUser;
import com.datatransformer.testutil.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonFileSourceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonFileSource source = new JsonFileSource(objectMapper);

    @Test
    void read_shouldReturnStreamOfRawUsers_whenFileIsValid(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("valid_users.json");
        TestUtil.createSampleJsonFile(tempFile, TestUtil.getSampleRawUserJson());

        // Act
        try (Stream<RawUser> result = source.read(tempFile)) {
            List<RawUser> users = result.toList();

            // Assert
            assertThat(users).hasSize(1);
            assertThat(users.get(0).id()).isEqualTo("1");
            assertThat(users.get(0).userPrincipalName()).isEqualTo("user1@example.com");
        }
    }

    @Test
    void read_shouldThrowException_whenFileIsInvalid(@TempDir Path tempDir) throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("invalid.json");
        TestUtil.createSampleJsonFile(tempFile, "{ \"value\": \"not-an-array\" }");

        // Act & Assert
        assertThatThrownBy(() -> source.read(tempFile))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Expected 'value' to be an array");
    }
}
