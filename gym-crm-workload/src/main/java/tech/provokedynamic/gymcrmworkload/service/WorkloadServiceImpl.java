package tech.provokedynamic.gymcrmworkload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;
import tech.provokedynamic.gymcrmworkload.document.MonthSummary;
import tech.provokedynamic.gymcrmworkload.document.TrainerWorkloadDocument;
import tech.provokedynamic.gymcrmworkload.document.YearSummary;
import tech.provokedynamic.gymcrmworkload.repository.TrainerWorkloadRepository;
import tech.provokedynamic.gymcrmworkload.validation.WorkloadEventValidator;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final TrainerWorkloadRepository workloadRepository;
    private final WorkloadEventValidator validator;

    @Override
    public void processWorkload(WorkloadEvent event, String transactionId) {
        // Transaction-level log: one line per inbound event.
        log.info("[txId={}] START processWorkload trainer='{}' action={} date={} duration={}min",
                transactionId, event.trainerUsername(), event.actionType(),
                event.trainingDate(), event.trainingDuration());

        validator.validate(event);
        log.debug("[txId={}] Event validated OK for trainer='{}'", transactionId, event.trainerUsername());

        TrainerWorkloadDocument document = workloadRepository.findByTrainerUsername(event.trainerUsername())
                .map(existing -> {
                    log.debug("[txId={}] Existing workload document found for trainer='{}', id={}",
                            transactionId, event.trainerUsername(), existing.getId());
                    return existing;
                })
                .orElseGet(() -> {
                    log.info("[txId={}] No workload document for trainer='{}' - creating new one",
                            transactionId, event.trainerUsername());
                    return new TrainerWorkloadDocument(
                            event.trainerUsername(), event.trainerFirstName(),
                            event.trainerLastName(), event.isActive());
                });

        document.setTrainerFirstName(event.trainerFirstName());
        document.setTrainerLastName(event.trainerLastName());
        document.setTrainerStatus(event.isActive());

        applyDuration(document, event, transactionId);

        workloadRepository.save(document);

        // Transaction-level log: one line closing the event.
        log.info("[txId={}] END processWorkload trainer='{}' - document saved (id={})",
                transactionId, event.trainerUsername(), document.getId());
    }

    private void applyDuration(TrainerWorkloadDocument document, WorkloadEvent event, String transactionId) {
        LocalDate date = event.trainingDate();
        int year = date.getYear();
        int month = date.getMonthValue();

        YearSummary yearSummary = document.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("[txId={}] Year {} not present for trainer='{}' - adding it",
                            transactionId, year, event.trainerUsername());
                    var newYear = new YearSummary(year, new java.util.ArrayList<>());
                    document.getYears().add(newYear);
                    return newYear;
                });

        MonthSummary monthSummary = yearSummary.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("[txId={}] Month {}-{} not present for trainer='{}' - adding it",
                            transactionId, year, month, event.trainerUsername());
                    var newMonth = new MonthSummary(month, 0);
                    yearSummary.getMonths().add(newMonth);
                    return newMonth;
                });

        int current = monthSummary.getTrainingsSummaryDuration();
        int updated = switch (event.actionType()) {
            case ADD -> current + event.trainingDuration();
            case DELETE -> Math.max(0, current - event.trainingDuration());
        };
        monthSummary.setTrainingsSummaryDuration(updated);

        // Operation-level log: the actual arithmetic that happened.
        log.info("[txId={}] Updated duration for trainer='{}' {}-{}: {} -> {} ({} {}min)",
                transactionId, event.trainerUsername(), year, month,
                current, updated, event.actionType(), event.trainingDuration());
    }

    @Override
    public TrainerWorkloadDocument getSummary(String trainerUsername) {
        log.debug("Looking up workload summary for trainer '{}'", trainerUsername);
        return workloadRepository.findByTrainerUsername(trainerUsername).orElse(null);
    }
}
