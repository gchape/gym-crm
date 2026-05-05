package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.TrainingRequest;
import tech.provokedynamic.gymcrm.entity.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingService {
    Training create(TrainingRequest.Create request);

    Optional<Training> findById(long id);

    List<Training> findAll();
}
