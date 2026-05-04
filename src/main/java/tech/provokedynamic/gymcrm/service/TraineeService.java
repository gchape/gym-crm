package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.annotations.Validate;
import tech.provokedynamic.gymcrm.dto.TraineeRequest;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    @Validate
    Trainee create(TraineeRequest.Create request);

    @Validate
    Trainee update(long id, TraineeRequest.Update request);

    void delete(long id);

    Optional<Trainee> findById(long id);

    List<Trainee> findAll();
}
