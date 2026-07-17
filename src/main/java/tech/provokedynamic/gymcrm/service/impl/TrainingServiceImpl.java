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
        var trainee = traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new UserDoesNotExistException(request.traineeUsername()));
        var trainer = trainerRepository.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new UserDoesNotExistException(request.trainerUsername()));

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
        var training = trainingRepository.findById(request.trainingId())
                .orElseThrow(() -> new TrainingNotFoundException(request.trainingId()));

        trainingRepository.delete(training);

        var trainer = training.getTrainer();
        registerAfterCommitWorkloadNotification(trainer, training.getTrainingDate(), training.getTrainingDuration(),
                WorkloadEvent.ActionType.DELETE);

        log.info("Cancelled training id={}", request.trainingId());
    }

    private void registerAfterCommitWorkloadNotification(
            Trainer trainer, java.time.LocalDate trainingDate, int trainingDuration,
            WorkloadEvent.ActionType actionType) {

        // Capture fields now — the entity/session is gone by the time
        // afterCommit() runs.
        var event = new WorkloadEvent(
                trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(),
                trainer.isActive(), trainingDate, trainingDuration, actionType
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                workloadEventPublisher.publish(event);
            }
        });
    }
}
