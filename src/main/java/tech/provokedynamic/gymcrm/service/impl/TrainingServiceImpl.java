package tech.provokedynamic.gymcrm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.annotation.Validate;
import tech.provokedynamic.gymcrm.client.WorkloadClient;
import tech.provokedynamic.gymcrm.client.WorkloadRequest;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.exception.TrainingNotFoundException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;
import tech.provokedynamic.gymcrm.service.TrainingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final WorkloadClient workloadClient;

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

        workloadClient.sendWorkload(new WorkloadRequest(
                trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(),
                trainer.isActive(), request.trainingDate(), request.trainingDuration(),
                WorkloadRequest.ActionType.ADD
        ));

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
        workloadClient.sendWorkload(new WorkloadRequest(
                trainer.getUsername(), trainer.getFirstName(), trainer.getLastName(),
                trainer.isActive(), training.getTrainingDate(), training.getTrainingDuration(),
                WorkloadRequest.ActionType.DELETE
        ));

        log.info("Cancelled training id={}", request.trainingId());
    }
}
