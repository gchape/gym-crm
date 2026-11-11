package tech.provokedynamic.gymcrm.dto.response;

import tech.provokedynamic.gymcrm.dto.UserProfile;


/** Public trainer details included in trainee profile and assignment responses. */
public record TrainerSummaryResponse(
        String username,
        String firstName,
        String lastName,
        String specialization
) {
    public static TrainerSummaryResponse from(UserProfile.Trainer profile) {
        return new TrainerSummaryResponse(
                profile.username(),
                profile.firstName(),
                profile.lastName(),
                profile.specialization()
        );
    }
}
