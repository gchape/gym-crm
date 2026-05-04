package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.entity.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(Trainee trainee);

    Trainee update(long id, Trainee trainee);

    void delete(long id);

    Optional<Trainee> findById(long id);

    List<Trainee> findAll();
}
