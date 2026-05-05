package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.TrainingRequest;
import tech.provokedynamic.gymcrm.dto.TrainingResponse;

import java.util.List;

public interface TrainingService {
    TrainingResponse.Detail create(TrainingRequest.Create request);

    TrainingResponse.Detail findById(long id);

    List<TrainingResponse.Summary> findAll();
}