package tech.provokedynamic.gymcrm.facade;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.TraineeRequest;
import tech.provokedynamic.gymcrm.dto.TrainerRequest;
import tech.provokedynamic.gymcrm.dto.TrainingRequest;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.service.TraineeService;
import tech.provokedynamic.gymcrm.service.TrainerService;
import tech.provokedynamic.gymcrm.service.TrainingService;

import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public Trainee createTrainee(TraineeRequest.Create request) {
        return traineeService.create(request);
    }

    public Trainee updateTrainee(long id, TraineeRequest.Update request) {
        return traineeService.update(id, request);
    }

    public void deleteTrainee(long id) {
        traineeService.delete(id);
    }

    public Optional<Trainee> findTraineeById(long id) {
        return traineeService.findById(id);
    }

    public List<Trainee> findAllTrainees() {
        return traineeService.findAll();
    }

    public Trainer createTrainer(TrainerRequest.Create request) {
        return trainerService.create(request);
    }

    public Trainer updateTrainer(long id, TrainerRequest.Update request) {
        return trainerService.update(id, request);
    }

    public Optional<Trainer> findTrainerById(long id) {
        return trainerService.findById(id);
    }

    public List<Trainer> findAllTrainers() {
        return trainerService.findAll();
    }

    public Training createTraining(TrainingRequest.Create request) {
        return trainingService.create(request);
    }

    public Optional<Training> findTrainingById(long id) {
        return trainingService.findById(id);
    }

    public List<Training> findAllTrainings() {
        return trainingService.findAll();
    }
}
