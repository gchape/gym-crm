package tech.provokedynamic.gymcrm.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.provokedynamic.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public record Training(
        @JsonProperty("traineeId") long traineeId,
        @JsonProperty("trainerId") long trainerId,
        @JsonProperty("trainingName") String trainingName,
        @JsonProperty("trainingType") TrainingType trainingType,
        @JsonProperty("trainingDate") LocalDate trainingDate,
        @JsonProperty("trainingDuration") Duration trainingDuration
) implements Entity {
}
