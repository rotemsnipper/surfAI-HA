package com.datatransformer.pipeline.interfaces;

import java.util.stream.Stream;

import java.io.IOException;

public interface Source<T, I> {
    Stream<T> read(I input) throws IOException;
}
