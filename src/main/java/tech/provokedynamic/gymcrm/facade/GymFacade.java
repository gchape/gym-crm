package tech.provokedynamic.gymcrm.facade;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.*;
import tech.provokedynamic.gymcrm.service.TraineeService;
import tech.provokedynamic.gymcrm.service.TrainerService;
import tech.provokedynamic.gymcrm.service.TrainingService;

import java.util.List;

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

    public TraineeResponse.Detail createTrainee(TraineeRequest.Create request) {
        return traineeService.create(request);
    }

    public TraineeResponse.Detail updateTrainee(long id, TraineeRequest.Update request) {
        return traineeService.update(id, request);
    }

    public void deleteTrainee(long id) {
        traineeService.delete(id);
    }

    public TraineeResponse.Detail findTraineeById(long id) {
        return traineeService.findById(id);
    }

    public List<TraineeResponse.Summary> findAllTrainees() {
        return traineeService.findAll();
    }

    public TrainerResponse.Detail createTrainer(TrainerRequest.Create request) {
        return trainerService.create(request);
    }

    public TrainerResponse.Detail updateTrainer(long id, TrainerRequest.Update request) {
        return trainerService.update(id, request);
    }

    public TrainerResponse.Detail findTrainerById(long id) {
        return trainerService.findById(id);
    }

    public List<TrainerResponse.Summary> findAllTrainers() {
        return trainerService.findAll();
    }

    public TrainingResponse.Detail createTraining(TrainingRequest.Create request) {
        return trainingService.create(request);
    }

    public TrainingResponse.Detail findTrainingById(long id) {
        return trainingService.findById(id);
    }

    public List<TrainingResponse.Summary> findAllTrainings() {
        return trainingService.findAll();
    }
}
