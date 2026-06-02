package tech.provokedynamic.gymcrm.service;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;

import java.time.LocalDate;
import java.util.List;

public interface TraineeService {

    Profile.Trainee create(Request.CreateTrainee request);

    Profile.Trainee getProfile(String username);

    void changePassword(Request.ChangePassword request);

    Profile.Trainee update(Request.UpdateTrainee request);

    void activate(Request.ToggleActive request);

    void deactivate(Request.ToggleActive request);

    void delete(Request.DeleteTrainee request);

    List<Summary.Training> getTrainings(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainerUsername,
            @Nullable String trainingType
    );

    List<Profile.Trainer> getUnassignedTrainers(String username);

    List<Profile.Trainer> updateTrainers(Request.UpdateTraineeTrainers request);
}
