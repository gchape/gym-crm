package tech.provokedynamic.gymcrm.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.time.DurationMin;
import tech.provokedynamic.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public abstract sealed class TrainingRequest implements Request permits TrainingRequest.Create {
    @Positive(message = "Trainee ID must be positive")
    private final long traineeId;

    @Positive(message = "Trainer ID must be positive")
    private final long trainerId;

    @NotBlank(message = "Training name is required")
    private final String trainingName;

    @NotNull(message = "Training type is required")
    private final TrainingType trainingType;

    @NotNull(message = "Training date is required")
    @FutureOrPresent(message = "Training date must be today or in the future")
    private final LocalDate trainingDate;

    @NotNull(message = "Training duration is required")
    @DurationMin(days = 0, hours = 0, minutes = 30, message = "Training must be at least 30 minutes")
    private final Duration trainingDuration;

    protected TrainingRequest(long traineeId, long trainerId, String trainingName,
                              TrainingType trainingType, LocalDate trainingDate,
                              Duration trainingDuration) {
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public long getTraineeId() {
        return traineeId;
    }

    public long getTrainerId() {
        return trainerId;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public TrainingType getTrainingType() {
        return trainingType;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public Duration getTrainingDuration() {
        return trainingDuration;
    }

    public static final class Create extends TrainingRequest {
        public Create(long traineeId, long trainerId, String trainingName,
                      TrainingType trainingType, LocalDate trainingDate,
                      Duration trainingDuration) {
            super(traineeId, trainerId, trainingName, trainingType, trainingDate, trainingDuration);
        }
    }
}
