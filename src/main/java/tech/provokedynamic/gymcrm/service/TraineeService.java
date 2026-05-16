package tech.provokedynamic.gymcrm.service;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;

import java.time.LocalDate;
import java.util.List;

public interface TraineeService {

    /**
     * Requirement #2
     */
    Profile.Trainee create(Request.CreateTrainee request);

    /**
     * Requirement #6
     */
    Profile.Trainee getProfile(String username);

    /**
     * Requirement #7
     */
    void changePassword(Request.ChangePassword request);

    /**
     * Requirement #10
     */
    Profile.Trainee update(Request.UpdateTrainee request);

    /**
     * Requirement #11 — throws if already active
     */
    void activate(Request.ToggleActive request);

    /**
     * Requirement #11 — throws if already inactive
     */
    void deactivate(Request.ToggleActive request);

    /**
     * Requirement #13 — hard delete, cascades to trainings
     */
    void delete(Request.DeleteTrainee request);

    /**
     * Requirement #14
     */
    List<Summary.Training> getTrainings(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainerUsername,
            @Nullable String trainingType
    );

    /**
     * Requirement #17
     */
    List<Profile.Trainer> getUnassignedTrainers(String username);

    /**
     * Requirement #18
     */
    List<Profile.Trainer> updateTrainers(Request.UpdateTraineeTrainers request);
}
