package tech.provokedynamic.gymcrm.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import tech.provokedynamic.gymcrm.model.Address;
import tech.provokedynamic.gymcrm.validation.PersonName;

import java.time.LocalDate;
import java.util.List;

public sealed interface Request permits Request.AddTraining, Request.CancelTraining, Request.ChangePassword, Request.CreateTrainee, Request.CreateTrainer, Request.DeleteTrainee, Request.Login, Request.ToggleActive, Request.UpdateTrainee, Request.UpdateTraineeTrainers, Request.UpdateTrainer {

    record CancelTraining(
            @NotNull(message = "Training id is required")
            Long trainingId
    ) implements Request {
    }

    record Login(
            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Password is required")
            String password
    ) implements Request {
    }

    record CreateTrainee(
            @PersonName
            String firstName,

            @PersonName
            String lastName,

            @Nullable
            @PastOrPresent(message = "Date of birth cannot be in the future")
            LocalDate dateOfBirth,

            @Nullable
            @Valid
            Address address
    ) implements Request {
    }

    record UpdateTrainee(
            @NotBlank(message = "Username is required")
            String username,

            @PersonName
            String firstName,

            @PersonName
            String lastName,

            @Nullable
            @PastOrPresent(message = "Date of birth cannot be in the future")
            LocalDate dateOfBirth,

            @Nullable
            @Valid
            Address address,

            @NotNull(message = "isActive is required")
            Boolean isActive
    ) implements Request {
    }

    record DeleteTrainee(
            @NotBlank(message = "Username is required")
            String username
    ) implements Request {
    }

    record UpdateTraineeTrainers(
            @NotBlank(message = "Username is required")
            String username,

            @NotNull(message = "Trainer list is required")
            @Size(min = 1, message = "At least one trainer username must be provided")
            List<@NotBlank(message = "Trainer username must not be blank") String> trainerUsernames
    ) implements Request {
    }

    record CreateTrainer(
            @PersonName
            String firstName,

            @PersonName
            String lastName,

            @NotBlank(message = "Specialization is required")
            String specialization
    ) implements Request {
    }

    record UpdateTrainer(
            @NotBlank(message = "Username is required")
            String username,

            @PersonName
            String firstName,

            @PersonName
            String lastName,

            @NotBlank(message = "Specialization is required")
            String specialization,

            @NotNull(message = "isActive is required")
            Boolean isActive
    ) implements Request {
    }

    record ChangePassword(
            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Current password is required")
            String password,

            @NotBlank(message = "New password is required")
            @Size(min = 10, max = 10, message = "New password must be exactly 10 characters")
            String newPassword
    ) implements Request {
    }

    record ToggleActive(
            @NotBlank(message = "Username is required")
            String username,

            @NotNull(message = "isActive is required")
            Boolean isActive
    ) implements Request {
    }

    record AddTraining(
            @NotBlank(message = "Trainee username is required")
            String traineeUsername,

            @NotBlank(message = "Trainer username is required")
            String trainerUsername,

            @NotBlank(message = "Training name is required")
            @Size(max = 100, message = "Training name must not exceed 100 characters")
            String trainingName,

            @NotNull(message = "Training date is required")
            @FutureOrPresent(message = "Training date cannot be in the past")
            LocalDate trainingDate,

            @NotNull(message = "Training duration is required")
            @Positive(message = "Training duration must be greater than zero")
            @Max(value = 480, message = "Training duration cannot exceed 480 minutes (8 hours)")
            Integer trainingDuration
    ) implements Request {
    }
}
