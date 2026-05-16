package tech.provokedynamic.gymcrm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.annotation.Authenticated;
import tech.provokedynamic.gymcrm.annotation.Validate;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.TrainingDao;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.service.TrainingService;

@Service
public class TrainingServiceImpl implements TrainingService {

    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;

    public TrainingServiceImpl(
            TrainingDao trainingDao,
            TraineeDao traineeDao,
            TrainerDao trainerDao,
            TrainingTypeDao trainingTypeDao
    ) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    @Validate
    @Authenticated
    @Transactional
    public void add(Request.AddTraining request) {
        var trainee = traineeDao.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new UserDoesNotExistException(request.traineeUsername()));

        var trainer = trainerDao.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new UserDoesNotExistException(request.trainerUsername()));

        var training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainingTypeDao.findByName(request.trainingType()))
                .trainingName(request.trainingName())
                .trainingDate(request.trainingDate())
                .trainingDuration(request.trainingDuration())
                .build();

        trainingDao.save(training);

        log.info("Added training '{}' for trainee '{}' with trainer '{}'",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());
    }
}
