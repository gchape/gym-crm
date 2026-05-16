package tech.provokedynamic.gymcrm.service;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;

import java.time.LocalDate;
import java.util.List;

public interface TrainerService {

    /**
     * Requirement #1
     */
    Profile.Trainer create(Request.CreateTrainer request);

    /**
     * Requirement #5
     */
    Profile.Trainer getProfile(String username);

    /**
     * Requirement #8
     */
    void changePassword(Request.ChangePassword request);

    /**
     * Requirement #9
     */
    Profile.Trainer update(Request.UpdateTrainer request);

    /**
     * Requirement #12 — throws if already active
     */
    void activate(Request.ToggleActive request);

    /**
     * Requirement #12 — throws if already inactive
     */
    void deactivate(Request.ToggleActive request);

    /**
     * Requirement #15
     */
    List<Summary.Training> getTrainings(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String traineeUsername
    );
}
