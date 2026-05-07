package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.TraineeRequest;
import tech.provokedynamic.gymcrm.dto.TraineeResponse;

import java.util.List;

public interface TraineeService {
    TraineeResponse.Detail create(TraineeRequest.Create request);

    TraineeResponse.Detail update(long id, TraineeRequest.Update request);

    void delete(long id);

    TraineeResponse.Detail findById(long id);

    List<TraineeResponse.Summary> findAll();
}
