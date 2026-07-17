package tech.provokedynamic.gymcrmworkload.service;

import tech.provokedynamic.gymcrmworkload.model.TrainerWorkloadSummary;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;

public interface WorkloadService {

    void processWorkload(WorkloadEvent event);

    TrainerWorkloadSummary getSummary(String trainerUsername);
}
