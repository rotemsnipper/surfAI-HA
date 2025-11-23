package com.datatransformer.component;

import com.datatransformer.pipeline.interfaces.Sink;
import com.datatransformer.model.TargetUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.stream.Stream;

@Component
public class JsonFileSink implements Sink<TargetUser, Path> {

    private final ObjectMapper objectMapper;

    public JsonFileSink(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void write(Stream<TargetUser> data, Path output) throws IOException {
        try (var writer = new java.io.BufferedWriter(
                new java.io.FileWriter(output.toFile()))) {
            data.forEach(user -> {
                try {
                    writer.write(objectMapper.writeValueAsString(user));
                    writer.newLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
