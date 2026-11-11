package tech.provokedynamic.gymcrm.dto.response;

import tech.provokedynamic.gymcrm.dto.UserProfile;

import java.util.List;

/** Trainer profile snapshot with specialization and assigned trainees. */
public record TrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        String specialization,
        boolean isActive,
        List<TraineeSummaryResponse> trainees
) {

    public static TrainerProfileResponse from(UserProfile.Trainer profile) {
        return new TrainerProfileResponse(
                profile.username(),
                profile.firstName(),
                profile.lastName(),
                profile.specialization(),
                profile.isActive(),
                profile.trainees().stream().map(TraineeSummaryResponse::from).toList()
        );
    }
}
