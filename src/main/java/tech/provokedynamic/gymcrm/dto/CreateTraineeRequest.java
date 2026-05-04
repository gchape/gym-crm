package tech.provokedynamic.gymcrm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

public record CreateTraineeRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Valid
        Address address
) implements Request {
}
