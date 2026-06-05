package tech.provokedynamic.gymcrm.service;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.dto.Summary;

import java.time.LocalDate;
import java.util.List;

public interface TrainerService {

    Response.CreatedUser create(Request.CreateTrainer request);

    Profile.Trainer getProfile(String username);

    void changePassword(Request.ChangePassword request);

    Profile.Trainer update(Request.UpdateTrainer request);

    void activate(Request.ToggleActive request);

    void deactivate(Request.ToggleActive request);

    List<Summary.Training> getTrainings(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String traineeUsername
    );
}
