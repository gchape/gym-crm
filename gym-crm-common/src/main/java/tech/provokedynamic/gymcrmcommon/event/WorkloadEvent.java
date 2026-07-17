package tech.provokedynamic.gymcrmcommon.event;

import java.time.LocalDate;

/**
 * Kafka message payload for trainer workload updates, published by gym-crm
 * and consumed by gym-crm-workload. Key on the topic is trainerUsername so
 * all events for a given trainer stay ordered on the same partition.
 */
public record WorkloadEvent(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean isActive,
        LocalDate trainingDate,
        int trainingDuration,
        ActionType actionType
) {
    public enum ActionType {ADD, DELETE}
}
