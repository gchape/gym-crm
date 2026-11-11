package tech.provokedynamic.gymcrm.service;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.UserProfile;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.dto.response.*;
import tech.provokedynamic.gymcrm.dto.TrainingSummary;

import java.time.LocalDate;
import java.util.List;

/** Application boundary for trainer profiles and their session queries. */
public interface TrainerService {

    RegistrationCredentials create(RegisterTrainerRequest request);

    UserProfile.Trainer getProfile(String username);

    void changePassword(ChangePasswordRequest request);

    UserProfile.Trainer update(UpdateTrainerRequest request);

    void activate(ChangeActiveStatusRequest request);

    void deactivate(ChangeActiveStatusRequest request);

    List<TrainingSummary> getTrainings(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String traineeUsername
    );
}
