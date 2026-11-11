package tech.provokedynamic.gymcrmworkload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;
import tech.provokedynamic.gymcrmworkload.repository.TrainerWorkloadRepository;
import tech.provokedynamic.gymcrmworkload.validation.WorkloadEventValidator;
import java.util.Optional;

/** Coordinates event validation, aggregate loading, and optimistic-locking persistence. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadApplicationService implements TrainerWorkloadService {
    private final TrainerWorkloadRepository workloadRepository;
    private final WorkloadEventValidator validator;

    @Override
    public void applyEvent(WorkloadEvent event, String transactionId) {
        validator.validate(event);
        var workload = workloadRepository.findByTrainerUsername(event.trainerUsername())
                .orElseGet(() -> new TrainerWorkloadDocument(event.trainerUsername(), event.trainerFirstName(),
                        event.trainerLastName(), event.isActive()));
        workload.applyEvent(event);
        workloadRepository.save(workload);
        log.info("[txId={}] Applied workload change: trainer={}, action={}, date={}, minutes={}",
                transactionId, event.trainerUsername(), event.actionType(), event.trainingDate(), event.trainingDuration());
    }

    @Override
    public Optional<TrainerWorkloadDocument> findWorkload(String trainerUsername) {
        return workloadRepository.findByTrainerUsername(trainerUsername);
    }
}
