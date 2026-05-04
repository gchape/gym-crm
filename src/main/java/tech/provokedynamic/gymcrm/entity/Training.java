package tech.provokedynamic.gymcrm.entity;

import tech.provokedynamic.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public record Training(
        long traineeId,
        long trainerId,
        String trainingName,
        TrainingType trainingType,
        LocalDate trainingDate,
        Duration trainingDuration
) implements Entity {
}
