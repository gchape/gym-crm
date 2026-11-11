package tech.provokedynamic.gymcrmworkload.service;

import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;
import java.util.Optional;

/** Application boundary for event-driven workload changes and persisted trainer projections. */
public interface TrainerWorkloadService {
    /** Applies a decoded event. Failures propagate to the listener's retry/dead-letter policy. */
    void applyEvent(WorkloadEvent event, String transactionId);

    /** Returns the persisted projection, or empty when this trainer has no workload history. */
    Optional<TrainerWorkloadDocument> findWorkload(String trainerUsername);
}
