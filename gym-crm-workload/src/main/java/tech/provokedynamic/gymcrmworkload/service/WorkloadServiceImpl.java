package tech.provokedynamic.gymcrmworkload.service;

import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrmworkload.dto.WorkloadRequest;
import tech.provokedynamic.gymcrmworkload.model.MonthSummary;
import tech.provokedynamic.gymcrmworkload.model.TrainerWorkloadSummary;
import tech.provokedynamic.gymcrmworkload.model.YearSummary;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkloadServiceImpl implements WorkloadService {

    private final Map<String, TrainerWorkloadSummary> workloadStore = new ConcurrentHashMap<>();

    @Override
    public void processWorkload(WorkloadRequest request) {
        TrainerWorkloadSummary summary = workloadStore.computeIfAbsent(
                request.trainerUsername(),
                username -> new TrainerWorkloadSummary(
                        username,
                        request.trainerFirstName(),
                        request.trainerLastName(),
                        request.isActive()
                )
        );

        summary.setTrainerFirstName(request.trainerFirstName());
        summary.setTrainerLastName(request.trainerLastName());
        summary.setTrainerStatus(request.isActive());

        LocalDate date = request.trainingDate();
        YearSummary yearSummary = summary.getOrCreateYear(date.getYear());
        MonthSummary monthSummary = yearSummary.getOrCreateMonth(date.getMonthValue());

        switch (request.actionType()) {
            case ADD -> monthSummary.addDuration(request.trainingDuration());
            case DELETE -> monthSummary.subtractDuration(request.trainingDuration());
        }
    }

    @Override
    public TrainerWorkloadSummary getSummary(String trainerUsername) {
        return workloadStore.get(trainerUsername);
    }
}
