package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.TraineeRequest;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.util.List;

public interface TraineeService {
    Trainee create(TraineeRequest.Create request);

    Trainee update(long id, TraineeRequest.Update request);

    void delete(long id);

    Trainee findById(long id);

    List<Trainee> findAll();
}