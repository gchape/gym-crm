package tech.provokedynamic.gymcrmworkload.mapper;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;
import tech.provokedynamic.gymcrmworkload.dto.response.MonthSummaryResponse;
import tech.provokedynamic.gymcrmworkload.dto.response.TrainerWorkloadResponse;
import tech.provokedynamic.gymcrmworkload.dto.response.YearSummaryResponse;

import java.util.Comparator;

@Component
public class WorkloadResponseMapper {

    public TrainerWorkloadResponse toResponse(TrainerWorkloadDocument document) {
        var years = document.getYears().stream()
                .sorted(Comparator.comparingInt(y -> y.getYear()))
                .map(y -> new YearSummaryResponse(
                        y.getYear(),
                        y.getMonths().stream()
                                .sorted(Comparator.comparingInt(m -> m.getMonth()))
                                .map(m -> new MonthSummaryResponse(m.getMonth(), m.getTrainingsSummaryDuration()))
                                .toList()))
                .toList();

        return new TrainerWorkloadResponse(
                document.getTrainerUsername(),
                document.getTrainerFirstName(),
                document.getTrainerLastName(),
                document.getTrainerStatus(),
                years
        );
    }
}
