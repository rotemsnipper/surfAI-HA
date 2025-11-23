package com.datatransformer.pipeline.interfaces;

import java.io.IOException;
import java.util.stream.Stream;

public interface Sink<T, O> {
    void write(Stream<T> dataStream, O destination) throws IOException;
}
