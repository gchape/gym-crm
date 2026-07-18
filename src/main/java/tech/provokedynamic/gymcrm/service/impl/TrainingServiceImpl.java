package tech.provokedynamic.gymcrm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.provokedynamic.gymcrm.annotation.Validate;
import tech.provokedynamic.gymcrm.client.WorkloadEventPublisher;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.exception.TrainingNotFoundException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;
import tech.provokedynamic.gymcrm.service.TrainingService;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final WorkloadEventPublisher workloadEventPublisher;

    @Override
    @Validate
    @Transactional
    public void add(Request.AddTraining request) {
        log.debug("Adding training '{}' for trainee '{}' with trainer '{}' on {} ({} min)",
                request.trainingName(), request.traineeUsername(), request.trainerUsername(),
                request.trainingDate(), request.trainingDuration());

        var trainee = traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> {
                    log.warn("Add training failed: trainee '{}' does not exist", request.traineeUsername());
                    return new UserDoesNotExistException(request.traineeUsername());
                });
        var trainer = trainerRepository.findByUsername(request.trainerUsername())
                .orElseThrow(() -> {
                    log.warn("Add training failed: trainer '{}' does not exist", request.trainerUsername());
                    return new UserDoesNotExistException(request.trainerUsername());
                });

        var training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainer.getSpecialization())
                .trainingName(request.trainingName())
                .trainingDate(request.trainingDate())
                .trainingDuration(request.trainingDuration())
                .build();

        trainingRepository.save(training);

        registerAfterCommitWorkloadNotification(trainer, request.trainingDate(), request.trainingDuration(),
                WorkloadEvent.ActionType.ADD);

        log.info("Added training '{}' for trainee '{}' with trainer '{}'",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());
    }

    @Override
    @Validate
    @Transactional
    public void cancel(Request.CancelTraining request) {
        log.debug("Cancelling training id={}", request.trainingId());

        var training = trainingRepository.findById(request.trainingId())
                .orElseThrow(() -> {
                    log.warn("Cancel training failed: id={} not found", request.trainingId());
                    return new TrainingNotFoundException(request.trainingId());
                });

        trainingRepository.delete(training);

        var trainer = training.getTrainer();
        registerAfterCommitWorkloadNotification(trainer, training.getTrainingDate(), training.getTrainingDuration(),
                WorkloadEvent.ActionType.DELETE);

        log.info("Cancelled training id={}, name='{}', trainer='{}'",
                request.trainingId(), training.getTrainingName(), trainer.getUsername());
    }

    private void registerAfterCommitWorkloadNotification(
            Trainer trainer, java.time.LocalDate trainingDate, int trainingDuration,
            WorkloadEvent.ActionType actionType) {

        var event = new WorkloadEvent(
                trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(),
                trainer.isActive(), trainingDate, trainingDuration, actionType
        );

        log.debug("Registering after-commit workload notification: trainer='{}', action={}",
                trainer.getUsername(), actionType);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                workloadEventPublisher.publish(event);
            }
        });
    }
}
