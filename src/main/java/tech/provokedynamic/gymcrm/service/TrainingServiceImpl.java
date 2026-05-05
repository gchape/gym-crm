package tech.provokedynamic.gymcrm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrm.annotations.Validate;
import tech.provokedynamic.gymcrm.dao.TrainingDao;
import tech.provokedynamic.gymcrm.dto.TrainingRequest;
import tech.provokedynamic.gymcrm.dto.TrainingResponse;
import tech.provokedynamic.gymcrm.entity.Training;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private final AtomicLong id = new AtomicLong(1);

    private final TrainingDao trainingDao;

    public TrainingServiceImpl(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    @Validate
    public TrainingResponse.Detail create(TrainingRequest.Create request) {
        long nextId = id.getAndIncrement();

        Training training = new Training(
                request.traineeId(),
                request.trainerId(),
                request.trainingName(),
                request.trainingType(),
                request.trainingDate(),
                request.trainingDuration()
        );

        log.debug("Creating training: {}", request.trainingName());
        return TrainingResponse.Detail.from(trainingDao.save(nextId, training));
    }

    @Override
    public TrainingResponse.Detail findById(long id) {
        log.debug("Finding training with id: {}", id);
        return TrainingResponse.Detail.from(
                trainingDao.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Training not found with id: " + id))
        );
    }

    @Override
    public List<TrainingResponse.Summary> findAll() {
        log.debug("Finding all trainings");
        return trainingDao.findAll().stream()
                .map(TrainingResponse.Summary::from)
                .toList();
    }
}