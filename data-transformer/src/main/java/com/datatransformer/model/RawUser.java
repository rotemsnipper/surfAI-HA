package com.datatransformer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawUser(
        String id,
        String userPrincipalName,
        String mail,
        String userType,
        String usageLocation,
        Boolean accountEnabled,
        String givenName,
        String surname,
        RawSignInActivity signInActivity) {
}
