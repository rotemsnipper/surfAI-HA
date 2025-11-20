package com.datatransformer.model.target;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record TargetSignInActivity() {
    String lastSignInDateTime,
    String lastSuccessfulSignInDateTime
}
