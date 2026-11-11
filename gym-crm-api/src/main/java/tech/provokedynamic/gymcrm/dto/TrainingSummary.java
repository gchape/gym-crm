package tech.provokedynamic.gymcrm.dto;

import java.time.LocalDate;

/** Read projection used by the legacy trainer and trainee session queries. */
public record TrainingSummary(String trainingName, LocalDate trainingDate, String trainingType,
                              Integer trainingDuration, String trainerUsername, String traineeUsername) {
}
