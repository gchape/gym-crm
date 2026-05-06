package tech.provokedynamic.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.provokedynamic.gymcrm.model.Specialization;

public sealed interface TrainerRequest extends Request permits TrainerRequest.Create, TrainerRequest.Update {

    String firstName();

    String lastName();

    Specialization specialization();

    record Create(
            @NotBlank(message = "First name is required")
            String firstName,

            @NotBlank(message = "Last name is required")
            String lastName,

            @NotNull(message = "Specialization is required")
            Specialization specialization
    ) implements TrainerRequest {
    }

    record Update(
            @NotBlank(message = "First name is required")
            String firstName,

            @NotBlank(message = "Last name is required")
            String lastName,

            @NotNull(message = "Specialization is required")
            Specialization specialization,

            boolean active
    ) implements TrainerRequest {
    }
}