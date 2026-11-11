package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;
import tech.provokedynamic.gymcrm.validation.PersonName;


/** Trainer registration details; specialization must refer to a catalog entry. */
public record RegisterTrainerRequest(
        @PersonName
        String firstName,

        @PersonName
        String lastName,

        @NotBlank(message = "Specialization is required")
        String specialization
) implements ValidatedRequest {
}
