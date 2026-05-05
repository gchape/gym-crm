package tech.provokedynamic.gymcrm.service;

import tech.provokedynamic.gymcrm.dto.TrainerRequest;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.util.List;

public interface TrainerService {
    Trainer create(TrainerRequest.Create request);

    Trainer update(long id, TrainerRequest.Update request);

    void delete(long id);

    Trainer findById(long id);

    List<Trainer> findAll();
}