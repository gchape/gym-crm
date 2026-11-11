package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;


/** Requested account activation state; repeated transitions are reported as conflicts. */
public record ChangeActiveStatusRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotNull(message = "isActive is required")
        Boolean isActive
) implements ValidatedRequest {
}
