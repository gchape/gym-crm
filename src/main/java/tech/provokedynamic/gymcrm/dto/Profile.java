package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public sealed interface Profile {

    record Trainee(
            String firstName,
            String lastName,
            String username,
            LocalDate dateOfBirth,
            Address address,
            boolean isActive,
            List<Trainer> trainers
    ) implements Profile {

        public static Profile.Trainee from(tech.provokedynamic.gymcrm.entity.Trainee trainee) {
            List<Trainer> trainerProfiles = trainee.getTrainers().stream()
                    .map(Profile.Trainer::fromEntity)
                    .collect(Collectors.toList());
            return new Profile.Trainee(
                    trainee.getFirstName(),
                    trainee.getLastName(),
                    trainee.getUsername(),
                    trainee.getDateOfBirth(),
                    trainee.getAddress(),
                    trainee.isActive(),
                    trainerProfiles
            );
        }

        public static Profile.Trainee fromEntity(tech.provokedynamic.gymcrm.entity.Trainee trainee) {
            return new Profile.Trainee(
                    trainee.getFirstName(),
                    trainee.getLastName(),
                    trainee.getUsername(),
                    trainee.getDateOfBirth(),
                    trainee.getAddress(),
                    trainee.isActive(),
                    List.of()
            );
        }
    }

    record Trainer(
            String firstName,
            String lastName,
            String username,
            String specialization,
            boolean isActive,
            List<Trainee> trainees
    ) implements Profile {

        public static Profile.Trainer from(tech.provokedynamic.gymcrm.entity.Trainer trainer) {
            List<Trainee> traineeProfiles = trainer.getTrainees().stream()
                    .map(Profile.Trainee::fromEntity)
                    .collect(Collectors.toList());
            return new Profile.Trainer(
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainer.getUsername(),
                    trainer.getSpecialization().getTrainingTypeName(),
                    trainer.isActive(),
                    traineeProfiles
            );
        }

        public static Profile.Trainer fromEntity(tech.provokedynamic.gymcrm.entity.Trainer trainer) {
            return new Profile.Trainer(
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainer.getUsername(),
                    trainer.getSpecialization().getTrainingTypeName(),
                    trainer.isActive(),
                    List.of()
            );
        }
    }
}
