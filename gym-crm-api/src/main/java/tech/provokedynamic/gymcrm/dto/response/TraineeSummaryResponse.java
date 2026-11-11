package tech.provokedynamic.gymcrm.dto.response;

import tech.provokedynamic.gymcrm.dto.UserProfile;


/** Public trainee details included in trainer profile responses. */
public record TraineeSummaryResponse(
        String username,
        String firstName,
        String lastName
) {
    public static TraineeSummaryResponse from(UserProfile.Trainee profile) {
        return new TraineeSummaryResponse(profile.username(), profile.firstName(), profile.lastName());
    }
}
