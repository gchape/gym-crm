package tech.provokedynamic.gymcrm.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import tech.provokedynamic.gymcrm.model.Address;
import tech.provokedynamic.gymcrm.validation.PersonName;

import java.time.LocalDate;

/** Self-registration details for a trainee; the application generates account credentials. */
public record RegisterTraineeRequest(
        @PersonName
        String firstName,

        @PersonName
        String lastName,

        @Nullable
        @PastOrPresent(message = "Date of birth cannot be in the future")
        LocalDate dateOfBirth,

        @Nullable
        @Valid
        Address address
) implements ValidatedRequest {
}
