package tech.provokedynamic.gymcrm.dto;

import java.time.LocalDate;

public sealed interface Summary {

    record Training(
            String trainingName,
            LocalDate trainingDate,
            Integer trainingDuration,
            String trainerUsername
    ) implements Summary {
    }
}
