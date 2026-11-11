package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;


/** Password-change command containing the current secret and its replacement. */
public record ChangePasswordRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Current password is required")
        String password,

        @NotBlank(message = "New password is required")
        @Size(min = 12, max = 72, message = "New password must be between 12 and 72 characters")
        String newPassword
) implements ValidatedRequest {
}
