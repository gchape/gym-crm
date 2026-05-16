package tech.provokedynamic.gymcrm.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import tech.provokedynamic.gymcrm.model.Address;

import java.time.LocalDate;
import java.util.List;

public sealed interface Request permits Request.AddTraining, Request.Authenticated, Request.ChangePassword, Request.CreateTrainee, Request.CreateTrainer, Request.DeleteTrainee, Request.ToggleActive, Request.UpdateTrainee, Request.UpdateTraineeTrainers, Request.UpdateTrainer {

    sealed interface Authenticated extends Request permits AddTraining, ChangePassword, DeleteTrainee, ToggleActive, UpdateTrainee, UpdateTraineeTrainers, UpdateTrainer {

        String username();

        String password();
    }

    record CreateTrainee(
            @NotBlank
            @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L}'-]+$", message = "First name must contain only letters, hyphens, or apostrophes")
            String firstName,

            @NotBlank
            @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L}'-]+$", message = "Last name must contain only letters, hyphens, or apostrophes")
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
            @NotBlank
            String username,

            @NotBlank
            String password,

            @NotBlank
            @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L}'-]+$", message = "First name must contain only letters, hyphens, or apostrophes")
            String firstName,

            @NotBlank
            @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L}'-]+$", message = "Last name must contain only letters, hyphens, or apostrophes")
            String lastName,

            @Nullable
            @PastOrPresent(message = "Date of birth cannot be in the future")
            LocalDate dateOfBirth,

            @Nullable
            @Valid
            Address address
    ) implements Request, Authenticated {
    }

    record DeleteTrainee(
            @NotBlank
            String username,

            @NotBlank
            String password
    ) implements Request, Authenticated {
    }

    record UpdateTraineeTrainers(
            @NotBlank
            String username,

            @NotBlank
            String password,

            @NotNull
            @Size(min = 1, message = "Trainer list must not be empty")
            List<@NotBlank String> trainerUsernames
    ) implements Request, Authenticated {
    }

    record CreateTrainer(
            @NotBlank
            @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L}'-]+$", message = "First name must contain only letters, hyphens, or apostrophes")
            String firstName,

            @NotBlank
            @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L}'-]+$", message = "Last name must contain only letters, hyphens, or apostrophes")
            String lastName,

            @NotBlank
            String specialization
    ) implements Request {
    }

    record UpdateTrainer(
            @NotBlank
            String username,

            @NotBlank
            String password,

            @NotBlank
            @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L}'-]+$", message = "First name must contain only letters, hyphens, or apostrophes")
            String firstName,

            @NotBlank
            @Size(min = 2, max = 50)
            @Pattern(regexp = "^[\\p{L}'-]+$", message = "Last name must contain only letters, hyphens, or apostrophes")
            String lastName,

            @NotBlank
            String specialization
    ) implements Request, Authenticated {
    }

    record ChangePassword(
            @NotBlank
            String username,

            @NotBlank
            String password,

            @NotBlank
            @Size(min = 10, max = 10, message = "Password must be exactly 10 characters")
            String newPassword
    ) implements Request, Authenticated {
    }

    record ToggleActive(
            @NotBlank
            String username,

            @NotBlank
            String password
    ) implements Request, Authenticated {
    }

    record AddTraining(
            @NotBlank
            String traineeUsername,

            @NotBlank
            String traineePassword,

            @NotBlank
            String trainerUsername,

            @NotBlank
            @Size(max = 100)
            String trainingName,

            @NotBlank
            String trainingType,

            @NotNull
            @FutureOrPresent(message = "Training date cannot be in the past")
            LocalDate trainingDate,

            @NotNull
            @Positive(message = "Training duration must be a positive number")
            @Max(value = 480, message = "Training duration cannot exceed 480 minutes")
            Integer trainingDuration
    ) implements Request, Authenticated {

        @Override
        public String username() {
            return traineeUsername();
        }

        @Override
        public String password() {
            return traineePassword();
        }
    }
}
