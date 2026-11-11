package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

/** Replacement trainer assignments; an empty list removes every assignment. */
public record ReplaceTrainerAssignmentsRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotNull(message = "Trainer list is required")
        List<@NotBlank(message = "Trainer username must not be blank") String> trainerUsernames
) implements ValidatedRequest {
}
