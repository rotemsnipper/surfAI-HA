package com.datatransformer.model.source;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawUser(
    String id,
    String userPrincipalName,
    String mail,
    String usageLocation,
    Boolean accountEnabled,
    String mobilePhone,
    String userType,
    String givenName,
    String surname,
    List<String> otherMails,
    RawSignInActivity signInActivity
) {}



