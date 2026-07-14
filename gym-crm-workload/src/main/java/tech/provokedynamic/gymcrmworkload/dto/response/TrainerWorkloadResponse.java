package tech.provokedynamic.gymcrmworkload.dto.response;

import java.util.List;

public record TrainerWorkloadResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean trainerStatus,
        List<YearSummaryResponse> years
) {
}
