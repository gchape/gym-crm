package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;

public sealed interface TrainingResponse extends Response permits TrainingResponse.Summary, TrainingResponse.Detail {

    record Summary(
            long traineeId,
            long trainerId,
            String trainingName,
            TrainingType trainingType
    ) implements TrainingResponse {
        public static Summary from(Training training) {
            return new Summary(
                    training.traineeId(),
                    training.trainerId(),
                    training.trainingName(),
                    training.trainingType()
            );
        }
    }

    record Detail(
            long traineeId,
            long trainerId,
            String trainingName,
            TrainingType trainingType,
            LocalDate trainingDate,
            Duration trainingDuration
    ) implements TrainingResponse {
        public static Detail from(Training training) {
            return new Detail(
                    training.traineeId(),
                    training.trainerId(),
                    training.trainingName(),
                    training.trainingType(),
                    training.trainingDate(),
                    training.trainingDuration()
            );
        }
    }
}
