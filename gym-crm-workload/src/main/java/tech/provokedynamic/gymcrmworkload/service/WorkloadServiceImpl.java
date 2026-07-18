package tech.provokedynamic.gymcrmworkload.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.model.TrainerWorkloadSummary;

import java.time.YearMonth;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WorkloadServiceImpl implements WorkloadService {

    private final Map<String, TrainerWorkloadSummary> workloadStore = new ConcurrentHashMap<>();

    @Override
    public void processWorkload(WorkloadEvent event) {
        log.debug("Processing workload event: trainer='{}', action={}, date={}, duration={}min",
                event.trainerUsername(), event.actionType(), event.trainingDate(), event.trainingDuration());

        boolean isNewTrainer = !workloadStore.containsKey(event.trainerUsername());

        TrainerWorkloadSummary summary = workloadStore.computeIfAbsent(
                event.trainerUsername(),
                username -> new TrainerWorkloadSummary(
                        username, event.trainerFirstName(), event.trainerLastName(), event.isActive())
        );

        if (isNewTrainer) {
            log.info("Creating new workload summary for trainer '{}'", event.trainerUsername());
        }

        summary.setTrainerFirstName(event.trainerFirstName());
        summary.setTrainerLastName(event.trainerLastName());
        summary.setTrainerStatus(event.isActive());

        YearMonth month = YearMonth.from(event.trainingDate());

        switch (event.actionType()) {
            case ADD -> summary.addDuration(month, event.trainingDuration());
            case DELETE -> summary.subtractDuration(month, event.trainingDuration());
        }

        log.info("Updated workload summary for trainer '{}': month={}, action={}, duration={}min",
                event.trainerUsername(), month, event.actionType(), event.trainingDuration());
    }

    @Override
    public TrainerWorkloadSummary getSummary(String trainerUsername) {
        log.debug("Looking up workload summary for trainer '{}'", trainerUsername);

        var summary = workloadStore.get(trainerUsername);

        if (summary == null) {
            log.debug("No workload summary found for trainer '{}'", trainerUsername);
        }

        return summary;
    }
}
