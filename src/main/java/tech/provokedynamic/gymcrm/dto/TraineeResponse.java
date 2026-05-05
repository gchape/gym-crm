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
                    trainee.getId(),
                    trainee.getUsername(),
                    trainee.getFirstName(),
                    trainee.getLastName(),
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
                    trainee.getId(),
                    trainee.getUsername(),
                    trainee.getFirstName(),
                    trainee.getLastName(),
                    trainee.isActive(),
                    trainee.getDateOfBirth(),
                    trainee.getAddress()
            );
        }
    }
}