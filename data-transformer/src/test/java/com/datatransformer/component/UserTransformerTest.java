package com.datatransformer.component;

import com.datatransformer.model.RawSignInActivity;
import com.datatransformer.model.RawUser;
import com.datatransformer.model.TargetUser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTransformerTest {

    private final UserTransformer transformer = new UserTransformer();

    @Test
    void transform_shouldMapAllFieldsCorrectly() {
        // Arrange
        RawSignInActivity rawActivity = new RawSignInActivity(
                "2023-01-01T10:00:00",
                "2023-01-01T10:00:00",
                "2023-01-01T09:00:00");

        RawUser rawUser = new RawUser(
                "id-123",
                "user@example.com",
                "user@example.com",
                "Member",
                "US",
                true,
                "John",
                "Doe",
                rawActivity);

        // Act
        TargetUser targetUser = transformer.transform(rawUser);

        // Assert
        assertThat(targetUser.id()).isEqualTo("id-123");
        assertThat(targetUser.externalId()).isEqualTo("user@example.com");
        assertThat(targetUser.mail()).isEqualTo("user@example.com");
        assertThat(targetUser.type()).isEqualTo("Member");
        assertThat(targetUser.location()).isEqualTo("US");
        assertThat(targetUser.isEnabled()).isTrue();
        assertThat(targetUser.firstName()).isEqualTo("John");
        assertThat(targetUser.lastName()).isEqualTo("Doe");

        assertThat(targetUser.lastSignInDateTime()).isEqualTo("2023-01-01T10:00:00");
        assertThat(targetUser.lastSuccessfulSignInDateTime()).isEqualTo("2023-01-01T10:00:00");
    }

    @Test
    void transform_shouldHandleNullSignInActivity() {
        // Arrange
        RawUser rawUser = new RawUser(
                "id-123",
                "user@example.com",
                null,
                "Member",
                "US",
                true,
                "John",
                "Doe",
                null);

        // Act
        TargetUser targetUser = transformer.transform(rawUser);

        // Assert
        assertThat(targetUser.lastSignInDateTime()).isNull();
        assertThat(targetUser.lastSuccessfulSignInDateTime()).isNull();
    }
}
