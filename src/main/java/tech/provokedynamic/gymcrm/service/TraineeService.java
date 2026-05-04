package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.CreateTraineeRequest;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(CreateTraineeRequest request);

    Trainee update(long id, CreateTraineeRequest request);

    void delete(long id);

    Optional<Trainee> findById(long id);

    List<Trainee> findAll();
}
