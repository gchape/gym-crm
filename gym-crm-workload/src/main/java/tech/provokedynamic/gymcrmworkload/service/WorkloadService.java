package tech.provokedynamic.gymcrmworkload.service;

import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;

public interface WorkloadService {

    void processWorkload(WorkloadEvent event, String transactionId);

    TrainerWorkloadDocument getSummary(String trainerUsername);
}
