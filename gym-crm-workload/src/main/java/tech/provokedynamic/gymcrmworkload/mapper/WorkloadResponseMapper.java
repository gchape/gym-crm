package tech.provokedynamic.gymcrmworkload.mapper;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmworkload.document.MonthlyWorkload;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;
import tech.provokedynamic.gymcrmworkload.document.YearlyWorkload;
import tech.provokedynamic.gymcrmworkload.dto.response.MonthSummaryResponse;
import tech.provokedynamic.gymcrmworkload.dto.response.TrainerWorkloadResponse;
import tech.provokedynamic.gymcrmworkload.dto.response.YearSummaryResponse;

import java.util.Comparator;

@Component
public class WorkloadResponseMapper {

    public TrainerWorkloadResponse toResponse(TrainerWorkloadDocument document) {
        var years = document.getYears().stream()
                .sorted(Comparator.comparingInt(YearlyWorkload::getYear))
                .map(y -> new YearSummaryResponse(
                        y.getYear(),
                        y.getMonths().stream()
                                .sorted(Comparator.comparingInt(MonthlyWorkload::getMonth))
                                .map(m -> new MonthSummaryResponse(m.getMonth(), m.getDurationMinutes()))
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
