package tech.provokedynamic.gymcrmworkload.service;

import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.model.TrainerWorkloadSummary;

import java.time.YearMonth;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WorkloadServiceImpl implements WorkloadService {

    private final Map<String, TrainerWorkloadSummary> workloadStore = new ConcurrentHashMap<>();

    @Override
    public void processWorkload(WorkloadEvent event) {
        TrainerWorkloadSummary summary = workloadStore.computeIfAbsent(
                event.trainerUsername(),
                username -> new TrainerWorkloadSummary(
                        username, event.trainerFirstName(), event.trainerLastName(), event.isActive())
        );

        summary.setTrainerFirstName(event.trainerFirstName());
        summary.setTrainerLastName(event.trainerLastName());
        summary.setTrainerStatus(event.isActive());

        YearMonth month = YearMonth.from(event.trainingDate());

        switch (event.actionType()) {
            case ADD -> summary.addDuration(month, event.trainingDuration());
            case DELETE -> summary.subtractDuration(month, event.trainingDuration());
        }
    }

    @Override
    public TrainerWorkloadSummary getSummary(String trainerUsername) {
        return workloadStore.get(trainerUsername);
    }
}
