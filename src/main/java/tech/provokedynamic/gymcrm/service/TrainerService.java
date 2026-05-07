package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.TrainerRequest;
import tech.provokedynamic.gymcrm.dto.TrainerResponse;

import java.util.List;

public interface TrainerService {
    TrainerResponse.Detail create(TrainerRequest.Create request);

    TrainerResponse.Detail update(long id, TrainerRequest.Update request);

    void delete(long id);

    TrainerResponse.Detail findById(long id);

    List<TrainerResponse.Summary> findAll();
}
