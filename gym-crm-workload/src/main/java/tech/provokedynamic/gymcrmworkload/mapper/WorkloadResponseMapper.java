package tech.provokedynamic.gymcrmworkload.mapper;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrmworkload.dto.response.MonthSummaryResponse;
import tech.provokedynamic.gymcrmworkload.dto.response.TrainerWorkloadResponse;
import tech.provokedynamic.gymcrmworkload.dto.response.YearSummaryResponse;
import tech.provokedynamic.gymcrmworkload.model.TrainerWorkloadSummary;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class WorkloadResponseMapper {

    public TrainerWorkloadResponse toResponse(TrainerWorkloadSummary summary) {
        Map<Integer, List<MonthSummaryResponse>> byYear = summary.getMonthlyDurations().entrySet().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getKey().getYear(),
                        TreeMap::new,
                        Collectors.mapping(
                                e -> new MonthSummaryResponse(e.getKey().getMonthValue(), e.getValue().get()),
                                Collectors.collectingAndThen(Collectors.toList(),
                                        list -> list.stream()
                                                .sorted(Comparator.comparingInt(MonthSummaryResponse::month))
                                                .toList())
                        )
                ));

        List<YearSummaryResponse> years = byYear.entrySet().stream()
                .map(e -> new YearSummaryResponse(e.getKey(), e.getValue()))
                .toList();

        return new TrainerWorkloadResponse(
                summary.getTrainerUsername(),
                summary.getTrainerFirstName(),
                summary.getTrainerLastName(),
                summary.isTrainerStatus(),
                years
        );
    }
}
