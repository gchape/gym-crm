package tech.provokedynamic.gymcrmworkload.dto.response;

import java.util.List;

public record YearSummaryResponse(
        int year,
        List<MonthSummaryResponse> months
) {
}
