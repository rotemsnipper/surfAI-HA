package com.datatransformer.component;

import com.datatransformer.pipeline.interfaces.Source;
import com.datatransformer.model.RawUser;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class JsonFileSource implements Source<RawUser, Path> {

    private final ObjectMapper objectMapper;

    public JsonFileSource(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Stream<RawUser> read(Path input) throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(input.toFile());
        try {
            // Advance to the "value" array
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Expected content to be an object");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                if ("value".equals(fieldName)) {
                    parser.nextToken(); // Move to START_ARRAY
                    if (parser.currentToken() != JsonToken.START_ARRAY) {
                        throw new IllegalStateException("Expected 'value' to be an array");
                    }

                    Iterator<RawUser> iterator = new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            try {
                                return parser.nextToken() != JsonToken.END_ARRAY;
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }

                        @Override
                        public RawUser next() {
                            try {
                                return objectMapper.readValue(parser, RawUser.class);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    };

                    return StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                            false).onClose(() -> {
                                try {
                                    parser.close();
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });
                } else {
                    parser.nextToken(); // Skip value
                    parser.skipChildren();
                }
            }
            parser.close();
            return Stream.empty();
        } catch (Exception e) {
            try {
                parser.close();
            } catch (IOException ex) {
                e.addSuppressed(ex);
            }
            throw e;
        }
    }
}
