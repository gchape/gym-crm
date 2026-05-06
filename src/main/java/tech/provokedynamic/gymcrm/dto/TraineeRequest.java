package tech.provokedynamic.gymcrm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

public sealed interface TraineeRequest extends Request permits TraineeRequest.Create, TraineeRequest.Update {

    String firstName();

    String lastName();

    LocalDate dateOfBirth();

    Address address();

    record Create(
            @NotBlank(message = "First name is required")
            String firstName,

            @NotBlank(message = "Last name is required")
            String lastName,

            @Past(message = "Date of birth must be in the past")
            LocalDate dateOfBirth,

            @Valid @NotNull(message = "Address is required")
            Address address
    ) implements TraineeRequest {
    }

    record Update(
            @NotBlank(message = "First name is required")
            String firstName,

            @NotBlank(message = "Last name is required")
            String lastName,

            @Past(message = "Date of birth must be in the past")
            LocalDate dateOfBirth,

            @Valid @NotNull(message = "Address is required")
            Address address,

            boolean active
    ) implements TraineeRequest {
    }
}