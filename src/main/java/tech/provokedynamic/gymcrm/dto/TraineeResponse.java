package tech.provokedynamic.gymcrm.dto;

import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;

public sealed interface TraineeResponse extends Response permits TraineeResponse.Summary, TraineeResponse.Detail {

    record Summary(
            long id,
            String username,
            String firstName,
            String lastName,
            boolean isActive
    ) implements TraineeResponse {
        public static Summary from(Trainee trainee) {
            return new Summary(
                    trainee.id(),
                    trainee.username(),
                    trainee.firstName(),
                    trainee.lastName(),
                    trainee.isActive()
            );
        }
    }

    record Detail(
            long id,
            String username,
            String firstName,
            String lastName,
            boolean isActive,
            LocalDate dateOfBirth,
            Address address
    ) implements TraineeResponse {
        public static Detail from(Trainee trainee) {
            return new Detail(
                    trainee.id(),
                    trainee.username(),
                    trainee.firstName(),
                    trainee.lastName(),
                    trainee.isActive(),
                    trainee.dateOfBirth(),
                    trainee.address()
            );
        }
    }
}
