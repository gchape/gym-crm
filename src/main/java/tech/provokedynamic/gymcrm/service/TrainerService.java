package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.CreateTrainerRequest;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(CreateTrainerRequest request);

    Trainer update(long id, CreateTrainerRequest request);

    Optional<Trainer> findById(long id);

    List<Trainer> findAll();
}
