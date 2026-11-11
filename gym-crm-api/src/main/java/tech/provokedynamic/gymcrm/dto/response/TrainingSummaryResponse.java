package tech.provokedynamic.gymcrm.dto.response;

import tech.provokedynamic.gymcrm.dto.TrainingSummary;

import java.time.LocalDate;

/** Legacy session-list response; new clients should use the paginated training API. */
public record TrainingSummaryResponse(
        String trainingName,
        LocalDate trainingDate,
        String trainingType,
        Integer trainingDuration,
        String trainerName,
        String traineeName
) {

    public static TrainingSummaryResponse fromTrainee(TrainingSummary training) {
        return new TrainingSummaryResponse(
                training.trainingName(),
                training.trainingDate(),
                training.trainingType(),
                training.trainingDuration(),
                training.trainerUsername(),
                null
        );
    }

    public static TrainingSummaryResponse fromTrainer(TrainingSummary training) {
        return new TrainingSummaryResponse(
                training.trainingName(),
                training.trainingDate(),
                training.trainingType(),
                training.trainingDuration(),
                null,
                training.traineeUsername()
        );
    }
}
