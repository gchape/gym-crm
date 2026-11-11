package tech.provokedynamic.gymcrm.dto.response;

import tech.provokedynamic.gymcrm.model.Address;
import tech.provokedynamic.gymcrm.dto.UserProfile;

import java.time.LocalDate;
import java.util.List;

/** Trainee profile snapshot with assigned trainers; contains no authentication secrets. */
public record TraineeProfileResponse(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Address address,
        boolean isActive,
        List<TrainerSummaryResponse> trainers
) {

    public static TraineeProfileResponse from(UserProfile.Trainee profile) {
        return new TraineeProfileResponse(
                profile.firstName(),
                profile.lastName(),
                profile.dateOfBirth(),
                profile.address(),
                profile.isActive(),
                profile.trainers().stream().map(TrainerSummaryResponse::from).toList()
        );
    }
}
