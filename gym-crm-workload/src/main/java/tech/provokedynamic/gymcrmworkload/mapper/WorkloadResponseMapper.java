package tech.provokedynamic.gymcrmworkload.mapper;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmworkload.dto.response.MonthSummaryResponse;
import tech.provokedynamic.gymcrmworkload.dto.response.TrainerWorkloadResponse;
import tech.provokedynamic.gymcrmworkload.dto.response.YearSummaryResponse;
import tech.provokedynamic.gymcrmworkload.model.MonthSummary;
import tech.provokedynamic.gymcrmworkload.model.TrainerWorkloadSummary;
import tech.provokedynamic.gymcrmworkload.model.YearSummary;

import java.util.Comparator;
import java.util.List;

@Component
public class WorkloadResponseMapper {

    public TrainerWorkloadResponse toResponse(TrainerWorkloadSummary summary) {
        List<YearSummaryResponse> years = summary.getYears().values().stream()
                .sorted(Comparator.comparingInt(YearSummary::getYear))
                .map(this::toYearResponse)
                .toList();

        return new TrainerWorkloadResponse(
                summary.getTrainerUsername(),
                summary.getTrainerFirstName(),
                summary.getTrainerLastName(),
                summary.isTrainerStatus(),
                years
        );
    }

    private YearSummaryResponse toYearResponse(YearSummary yearSummary) {
        List<MonthSummaryResponse> months = yearSummary.getMonths().values().stream()
                .sorted(Comparator.comparingInt(MonthSummary::getMonth))
                .map(m -> new MonthSummaryResponse(m.getMonth(), m.getTrainingSummaryDuration()))
                .toList();

        return new YearSummaryResponse(yearSummary.getYear(), months);
    }
}
