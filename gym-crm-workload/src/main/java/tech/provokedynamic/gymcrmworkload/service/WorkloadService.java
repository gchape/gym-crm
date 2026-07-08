package tech.provokedynamic.gymcrmworkload.service;

import tech.provokedynamic.gymcrmworkload.dto.WorkloadRequest;
import tech.provokedynamic.gymcrmworkload.model.TrainerWorkloadSummary;

public interface WorkloadService {

    void processWorkload(WorkloadRequest request);

    TrainerWorkloadSummary getSummary(String trainerUsername);
}
