package tech.provokedynamic.gymcrm.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import tech.provokedynamic.gymcrm.model.Address;
import tech.provokedynamic.gymcrm.validation.PersonName;

import java.time.LocalDate;

/** Editable trainee profile fields. Account activation also has a dedicated endpoint. */
public record UpdateTraineeRequest(
        @NotBlank(message = "Username is required")
        String username,

        @PersonName
        String firstName,

        @PersonName
        String lastName,

        @Nullable
        @PastOrPresent(message = "Date of birth cannot be in the future")
        LocalDate dateOfBirth,

        @Nullable
        @Valid
        Address address,

        @NotNull(message = "isActive is required")
        Boolean isActive
) implements ValidatedRequest {
}
