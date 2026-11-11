package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;


/** Identifies the trainee account whose sessions must be removed from workload totals. */
public record DeleteTraineeRequest(
        @NotBlank(message = "Username is required")
        String username
) implements ValidatedRequest {
}
