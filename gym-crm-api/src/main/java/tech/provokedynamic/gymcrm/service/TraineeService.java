package tech.provokedynamic.gymcrm.service;

import jakarta.annotation.Nullable;
import tech.provokedynamic.gymcrm.dto.UserProfile;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.dto.response.*;
import tech.provokedynamic.gymcrm.dto.TrainingSummary;

import java.time.LocalDate;
import java.util.List;

/** Application boundary for trainee account management and trainer assignments. */
public interface TraineeService {

    RegistrationCredentials create(RegisterTraineeRequest request);

    UserProfile.Trainee getProfile(String username);

    void changePassword(ChangePasswordRequest request);

    UserProfile.Trainee update(UpdateTraineeRequest request);

    void activate(ChangeActiveStatusRequest request);

    void deactivate(ChangeActiveStatusRequest request);

    void delete(DeleteTraineeRequest request);

    List<TrainingSummary> getTrainings(
            String username,
            @Nullable LocalDate from,
            @Nullable LocalDate to,
            @Nullable String trainerUsername,
            @Nullable String trainingType
    );

    List<UserProfile.Trainer> getUnassignedTrainers(String username);

    List<UserProfile.Trainer> updateTrainers(ReplaceTrainerAssignmentsRequest request);
}
