package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.model.Specialization;

public sealed interface TrainerResponse extends Response permits TrainerResponse.Summary, TrainerResponse.Detail {

    record Summary(
            long id,
            String username,
            String firstName,
            String lastName,
            boolean isActive
    ) implements TrainerResponse {
        public static Summary from(Trainer trainer) {
            return new Summary(
                    trainer.getId(),
                    trainer.getUsername(),
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainer.isActive()
            );
        }
    }

    record Detail(
            long id,
            String username,
            String firstName,
            String lastName,
            boolean isActive,
            Specialization specialization
    ) implements TrainerResponse {
        public static Detail from(Trainer trainer) {
            return new Detail(
                    trainer.getId(),
                    trainer.getUsername(),
                    trainer.getFirstName(),
                    trainer.getLastName(),
                    trainer.isActive(),
                    trainer.getSpecialization()
            );
        }
    }
}