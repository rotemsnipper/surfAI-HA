package com.datatransformer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawSignInActivity(
                String lastSignInDateTime,
                String lastSuccessfulSignInDateTime,
                String lastNonInteractiveSignInDateTime) {
}
