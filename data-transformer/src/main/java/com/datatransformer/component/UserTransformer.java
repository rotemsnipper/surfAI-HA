package com.datatransformer.component;

import com.datatransformer.pipeline.interfaces.Transformer;
import com.datatransformer.model.RawSignInActivity;
import com.datatransformer.model.RawUser;
import com.datatransformer.model.TargetUser;
import org.springframework.stereotype.Component;

@Component
public class UserTransformer implements Transformer<RawUser, TargetUser> {

    @Override
    public TargetUser transform(RawUser input) {
        RawSignInActivity signInActivity = input.signInActivity();

        return new TargetUser(
                input.id(),
                input.userPrincipalName(), // Mapped to external_id
                input.mail(),
                input.userType(),
                input.usageLocation(),
                input.accountEnabled(),
                input.givenName(), // Mapped to first_name
                input.surname(), // Mapped to last_name
                signInActivity != null ? signInActivity.lastSignInDateTime() : null,
                signInActivity != null ? signInActivity.lastSuccessfulSignInDateTime() : null);
    }
}
