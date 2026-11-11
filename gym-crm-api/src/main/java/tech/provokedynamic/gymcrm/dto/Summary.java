package tech.provokedynamic.gymcrm.dto;

import java.time.LocalDate;

public sealed interface Summary {

    record Training(
            String trainingName,
            LocalDate trainingDate,
            String trainingType,
            Integer trainingDuration,
            String trainerUsername,
            String traineeUsername
    ) implements Summary {
    }
}
