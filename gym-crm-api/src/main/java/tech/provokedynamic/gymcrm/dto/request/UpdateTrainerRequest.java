package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;
import tech.provokedynamic.gymcrm.validation.PersonName;


/** Editable trainer profile fields and specialization. */
public record UpdateTrainerRequest(
        @NotBlank(message = "Username is required")
        String username,

        @PersonName
        String firstName,

        @PersonName
        String lastName,

        @NotBlank(message = "Specialization is required")
        String specialization,

        @NotNull(message = "isActive is required")
        Boolean isActive
) implements ValidatedRequest {
}
