package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

public sealed interface Response {

    record TraineeProfile(
            String firstName,
            String lastName,
            String username,
            LocalDate dateOfBirth,
            Address address
    ) implements Response {
    }

    record TrainerProfile(
            String firstName,
            String lastName,
            String username,
            String specialization
    ) implements Response {
    }

    record TrainingProfile(
            String trainingName,
            LocalDate trainingDate,
            Integer trainingDuration,
            String trainerUsername
    ) implements Response {
    }
}
