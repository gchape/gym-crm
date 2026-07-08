package tech.provokedynamic.gymcrm.client;

import java.time.LocalDate;

public record WorkloadRequest(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        LocalDate trainingDate,
        Integer trainingDuration,
        ActionType actionType
) {
    public enum ActionType {ADD, DELETE}
}
