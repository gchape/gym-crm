package tech.provokedynamic.gymcrm.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** Schedules a date-based session. Duration is expressed in minutes and capped at eight hours. */
public record ScheduleTrainingRequest(
        @NotBlank(message = "Trainee username is required")
        String traineeUsername,

        @NotBlank(message = "Trainer username is required")
        String trainerUsername,

        @NotBlank(message = "Training name is required")
        @Size(max = 100, message = "Training name must not exceed 100 characters")
        String trainingName,

        @NotNull(message = "Training date is required")
        @FutureOrPresent(message = "Training date cannot be in the past")
        LocalDate trainingDate,

        @NotNull(message = "Training duration is required")
        @Positive(message = "Training duration must be greater than zero")
        @Max(value = 480, message = "Training duration cannot exceed 480 minutes (8 hours)")
        Integer trainingDuration
) implements ValidatedRequest {
}
