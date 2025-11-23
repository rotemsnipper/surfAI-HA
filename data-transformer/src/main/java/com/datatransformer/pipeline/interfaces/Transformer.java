package com.datatransformer.pipeline.interfaces;

@FunctionalInterface
public interface Transformer<I, O> {
    O transform(I input);
}
