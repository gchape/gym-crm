package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;


/** Credentials submitted for JWT authentication. Passwords must never be written to logs. */
public record LoginRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) implements ValidatedRequest {
}
