package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.List;

public sealed interface Response {

    record CreatedUser(
            String username,
            String password
    ) implements Response {
    }

    record TrainerSummary(
            String username,
            String firstName,
            String lastName,
            String specialization
    ) {
        public static TrainerSummary from(Profile.Trainer profile) {
            return new TrainerSummary(
                    profile.username(),
                    profile.firstName(),
                    profile.lastName(),
                    profile.specialization()
            );
        }
    }

    record TraineeSummary(
            String username,
            String firstName,
            String lastName
    ) {
        public static TraineeSummary from(Profile.Trainee profile) {
            return new TraineeSummary(profile.username(), profile.firstName(), profile.lastName());
        }
    }

    record TraineeProfile(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            Address address,
            boolean isActive,
            List<TrainerSummary> trainers
    ) implements Response {

        public static TraineeProfile from(Profile.Trainee profile) {
            return new TraineeProfile(
                    profile.firstName(),
                    profile.lastName(),
                    profile.dateOfBirth(),
                    profile.address(),
                    profile.isActive(),
                    profile.trainers().stream().map(TrainerSummary::from).toList()
            );
        }
    }

    record TrainerProfile(
            String username,
            String firstName,
            String lastName,
            String specialization,
            boolean isActive,
            List<TraineeSummary> trainees
    ) implements Response {

        public static TrainerProfile from(Profile.Trainer profile) {
            return new TrainerProfile(
                    profile.username(),
                    profile.firstName(),
                    profile.lastName(),
                    profile.specialization(),
                    profile.isActive(),
                    profile.trainees().stream().map(TraineeSummary::from).toList()
            );
        }
    }

    record TrainingSummary(
            String trainingName,
            LocalDate trainingDate,
            String trainingType,
            Integer trainingDuration,
            String trainerName,
            String traineeName
    ) implements Response {

        public static TrainingSummary fromTrainee(Summary.Training training) {
            return new TrainingSummary(
                    training.trainingName(),
                    training.trainingDate(),
                    training.trainingType(),
                    training.trainingDuration(),
                    training.trainerUsername(),
                    null
            );
        }

        public static TrainingSummary fromTrainer(Summary.Training training) {
            return new TrainingSummary(
                    training.trainingName(),
                    training.trainingDate(),
                    training.trainingType(),
                    training.trainingDuration(),
                    null,
                    training.traineeUsername()
            );
        }
    }

    record TrainingType(Long id, String trainingTypeName) implements Response {
    }
}
