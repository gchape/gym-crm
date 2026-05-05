package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.TrainerRequest;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(TrainerRequest.Create request);

    Trainer update(long id, TrainerRequest.Update request);

    Optional<Trainer> findById(long id);

    List<Trainer> findAll();
}
