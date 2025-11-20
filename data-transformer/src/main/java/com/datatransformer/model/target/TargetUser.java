package com.datatransformer.model.target;

@JsonPropertyOrder({
    "id",
    "external_id",
    "mail",
    "type",
    "location",
    "is_enabled",
    "first_name",
    "last_name"
})

public record TargetUser(
    @JsonProperty("id") 
        String id,
    @JsonProperty("external_id") 
        String externalId,
    @JsonProperty("mail") 
        String mail,
    @JsonProperty("type") 
        String type,
    @JsonProperty("location") 
        String location,
    @JsonProperty("is_enabled") 
        boolean isEnabled,
    @JsonProperty("first_name") 
        String firstName,
    @JsonProperty("last_name") 
        String lastName
)
