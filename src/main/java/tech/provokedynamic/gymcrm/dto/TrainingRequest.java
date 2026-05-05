package tech.provokedynamic.gymcrm.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;
import tech.provokedynamic.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public sealed interface TrainingRequest extends Request permits TrainingRequest.Create {

    long traineeId();

    long trainerId();

    String trainingName();

    TrainingType trainingType();

    LocalDate trainingDate();

    Duration trainingDuration();

    record Create(
            @Positive(message = "Trainee ID must be positive") long traineeId,
            @Positive(message = "Trainer ID must be positive") long trainerId,
            @NotBlank(message = "Training name is required") String trainingName,
            @NotNull(message = "Training type is required") TrainingType trainingType,
            @NotNull(message = "Training date is required")
            @FutureOrPresent(message = "Training date must be today or in the future") LocalDate trainingDate,
            @NotNull(message = "Training duration is required")
            @DurationMin(days = 0, hours = 0, minutes = 30, message = "Training must be at least 30 minutes") Duration trainingDuration
    ) implements TrainingRequest {
    }
}