package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

public sealed interface Profile {

    record Trainee(
            String firstName,
            String lastName,
            String username,
            LocalDate dateOfBirth,
            Address address
    ) implements Profile {
        public static Profile.Trainee from(tech.provokedynamic.gymcrm.entity.Trainee trainee) {
            return new Profile.Trainee(
                    trainee.getFirstName(),
                    trainee.getLastName(),
                    trainee.getUsername(),
                    trainee.getDateOfBirth(),
                    trainee.getAddress()
            );
        }
    }

    record Trainer(
            String firstName,
            String lastName,
            String username,
            String specialization
    ) implements Profile {
        public static Profile.Trainer from(tech.provokedynamic.gymcrm.entity.Trainer trainer) {
            return new Profile.Trainer(
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainer.getUsername(),
                    trainer.getSpecialization().getTrainingTypeName()
            );
        }
    }
}
