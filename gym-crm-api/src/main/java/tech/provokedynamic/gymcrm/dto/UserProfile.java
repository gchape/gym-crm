package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Internal profile projections assembled inside transactions to keep persistence entities out of HTTP responses. */
public sealed interface UserProfile {

    record Trainee(
            String firstName,
            String lastName,
            String username,
            LocalDate dateOfBirth,
            Address address,
            boolean isActive,
            List<Trainer> trainers
    ) implements UserProfile {

        public static UserProfile.Trainee from(tech.provokedynamic.gymcrm.entity.Trainee trainee) {
            List<Trainer> trainerProfiles = trainee.getTrainers().stream()
                    .map(UserProfile.Trainer::fromEntity)
                    .collect(Collectors.toList());
            return new UserProfile.Trainee(
                    trainee.getFirstName(),
                    trainee.getLastName(),
                    trainee.getUsername(),
                    trainee.getDateOfBirth(),
                    trainee.getAddress(),
                    trainee.isActive(),
                    trainerProfiles
            );
        }

        public static UserProfile.Trainee fromEntity(tech.provokedynamic.gymcrm.entity.Trainee trainee) {
            return new UserProfile.Trainee(
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
    ) implements UserProfile {

        public static UserProfile.Trainer from(tech.provokedynamic.gymcrm.entity.Trainer trainer) {
            List<Trainee> traineeProfiles = trainer.getTrainees().stream()
                    .map(UserProfile.Trainee::fromEntity)
                    .collect(Collectors.toList());
            return new UserProfile.Trainer(
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainer.getUsername(),
                    trainer.getSpecialization().getTrainingTypeName(),
                    trainer.isActive(),
                    traineeProfiles
            );
        }

        public static UserProfile.Trainer fromEntity(tech.provokedynamic.gymcrm.entity.Trainer trainer) {
            return new UserProfile.Trainer(
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
