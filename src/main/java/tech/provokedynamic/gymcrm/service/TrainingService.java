package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.TrainingRequest;
import tech.provokedynamic.gymcrm.entity.Training;

import java.util.List;

public interface TrainingService {
    Training create(TrainingRequest.Create request);

    Training findById(long id);

    List<Training> findAll();
}