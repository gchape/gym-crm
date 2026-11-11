package tech.provokedynamic.gymcrm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.annotation.ValidateRequest;
import tech.provokedynamic.gymcrm.client.WorkloadEventPublisher;
import tech.provokedynamic.gymcrm.dto.*;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.dto.response.*;
import tech.provokedynamic.gymcrm.repository.TrainingSpecifications;
import org.springframework.security.access.AccessDeniedException;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.exception.TrainingNotFoundException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;
import tech.provokedynamic.gymcrmcommon.event.WorkloadEvent;

/** Coordinates session persistence and post-commit workload notifications within relational transactions. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingApplicationService implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final WorkloadEventPublisher workloadEventPublisher;

    @Override
    @ValidateRequest
    @Transactional
    public TrainingDetails scheduleTraining(ScheduleTrainingRequest request) {
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

        training = trainingRepository.save(training);

        publishAfterCommit(trainer, request.trainingDate(), request.trainingDuration(),
                WorkloadEvent.ActionType.ADD);

        log.info("Added training '{}' for trainee '{}' with trainer '{}'",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());
        return TrainingDetails.from(training);
    }

    @Override
    @ValidateRequest
    @Transactional
    public void cancelTraining(CancelTrainingRequest request, String trainerUsername) {
        log.debug("Cancelling training id={}", request.trainingId());

        var training = trainingRepository.findById(request.trainingId())
                .orElseThrow(() -> {
                    log.warn("Cancel training failed: id={} not found", request.trainingId());
                    return new TrainingNotFoundException(request.trainingId());
                });

        if (!training.getTrainer().getUsername().equals(trainerUsername)) {
            throw new AccessDeniedException("Only the assigned trainer can cancel this training");
        }
        trainingRepository.delete(training);

        var trainer = training.getTrainer();
        publishAfterCommit(trainer, training.getTrainingDate(), training.getTrainingDuration(),
                WorkloadEvent.ActionType.DELETE);

        log.info("Cancelled training id={}, name='{}', trainer='{}'",
                request.trainingId(), training.getTrainingName(), trainer.getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingDetails getTraining(long id, String username) {
        var training = trainingRepository.findById(id)
                .orElseThrow(() -> new TrainingNotFoundException(id));
        if (!training.getTrainer().getUsername().equals(username)
                && !training.getTrainee().getUsername().equals(username)) {
            throw new AccessDeniedException("Only training participants can view this training");
        }
        return TrainingDetails.from(training);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TrainingDetails> searchTrainings(String username, TrainingSearch search) {
        return PageResponse.from(trainingRepository.findAll(TrainingSpecifications.visibleTo(username, search),
                search.pageable()).map(TrainingDetails::from));
    }

    private void publishAfterCommit(
            Trainer trainer, java.time.LocalDate trainingDate, int trainingDuration,
            WorkloadEvent.ActionType actionType) {

        var event = new WorkloadEvent(
                trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(),
                trainer.isActive(), trainingDate, trainingDuration, actionType
        );

        workloadEventPublisher.publishAfterCommit(event);
    }
}
