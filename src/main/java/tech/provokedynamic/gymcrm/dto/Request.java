package tech.provokedynamic.gymcrm.dto;

public sealed interface Request permits TrainerRequest, TraineeRequest, TrainingRequest {
}
