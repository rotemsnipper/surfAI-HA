package com.datatransformer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TargetSignInActivity(
                String lastSignInDateTime,
                String lastSuccessfulSignInDateTime,
                String lastNonInteractiveSignInDateTime) {
}
