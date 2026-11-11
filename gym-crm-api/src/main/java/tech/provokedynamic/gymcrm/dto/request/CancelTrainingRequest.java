package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;


/** Identifies a persisted session to cancel; identifiers must be positive. */
public record CancelTrainingRequest(
        @NotNull(message = "Training id is required")
        @Positive(message = "Training id must be positive")
        Long trainingId
) implements ValidatedRequest {
}
