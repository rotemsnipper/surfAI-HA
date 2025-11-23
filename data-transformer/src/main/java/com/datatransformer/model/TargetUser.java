package com.datatransformer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TargetUser(
        @JsonProperty("Id") String id,
        @JsonProperty("external_id") String externalId,
        String mail,
        String type,
        String location,
        @JsonProperty("is_enabled") Boolean isEnabled,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") String lastName,
        String lastSignInDateTime,
        String lastSuccessfulSignInDateTime) {
}
