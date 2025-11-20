package com.datatransformer.model.source;


@JsonIgnoreProperties(ignoreUnknown = true)
public record RawSignInActivity(
    String lastSignInDateTime,
    String lastSignInRequestId,
    String lastNonInteractiveSignInDateTime,
    String lastNonInteractiveSignInRequestId,
    String lastSuccessfulSignInDateTime,
    String lastSuccessfulSignInRequestId
) {}
