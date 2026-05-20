package tech.provokedynamic.gymcrm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.annotation.Authenticated;
import tech.provokedynamic.gymcrm.annotation.Validate;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.exception.TrainingTypeNotFoundException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;
import tech.provokedynamic.gymcrm.service.TrainingService;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingServiceImpl(
            TrainingRepository trainingRepository,
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            TrainingTypeRepository trainingTypeRepository
    ) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void add(Request.AddTraining request) {
        log.debug("Adding training '{}' for trainee '{}' with trainer '{}'",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());

        var trainee = traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new UserDoesNotExistException(request.traineeUsername()));

        var trainer = trainerRepository.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new UserDoesNotExistException(request.trainerUsername()));

        var trainingType = trainingTypeRepository.findByTrainingTypeName(request.trainingType())
                .orElseThrow(() -> new TrainingTypeNotFoundException(request.trainingType()));

        var training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingType)
                .trainingName(request.trainingName())
                .trainingDate(request.trainingDate())
                .trainingDuration(request.trainingDuration())
                .build();

        trainingRepository.save(training);

        log.info("Added training '{}' for trainee '{}' with trainer '{}'",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());
    }
}
