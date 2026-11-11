package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.entity.Training;
import java.time.LocalDate;

public record TrainingDetails(Long id, String trainingName, LocalDate trainingDate,
                              int trainingDuration, String trainingType,
                              String traineeUsername, String trainerUsername) {
    public static TrainingDetails from(Training training) {
        return new TrainingDetails(training.getId(), training.getTrainingName(), training.getTrainingDate(),
                training.getTrainingDuration(), training.getTrainingType().getTrainingTypeName(),
                training.getTrainee().getUsername(), training.getTrainer().getUsername());
    }
}
