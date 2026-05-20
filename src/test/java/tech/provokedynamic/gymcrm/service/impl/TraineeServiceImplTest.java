package tech.provokedynamic.gymcrm.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.model.Address;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeServiceImpl")
class TraineeServiceImplTest {

    @Mock
    TraineeRepository traineeRepository;

    @Mock
    TrainerRepository trainerRepository;

    @Mock
    CredentialGenerator credentialGenerator;

    @InjectMocks
    TraineeServiceImpl service;

    private Trainee buildTrainee() {
        return Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("secret")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address(new Address("123 Main St", "New York", "USA", "10001"))
                .build();
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("generates credentials, persists trainee, and returns profile")
        void create_success() {
            var request = new Request.CreateTrainee("John", "Doe",
                    LocalDate.of(1990, 1, 1), new Address("123 Main St", "New York", "USA", "10001"));

            when(credentialGenerator.generateUsername("John", "Doe"))
                    .thenReturn("John.Doe");
            when(credentialGenerator.generatePassword())
                    .thenReturn("pass123");
            when(traineeRepository.save(any(Trainee.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Profile.Trainee profile = service.create(request);

            assertThat(profile.username()).isEqualTo("John.Doe");
            verify(traineeRepository).save(any(Trainee.class));
        }
    }

    @Nested
    @DisplayName("getProfile()")
    class GetProfile {

        @Test
        @DisplayName("returns profile when trainee exists")
        void getProfile_found() {
            var trainee = buildTrainee();
            when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));

            Profile.Trainee profile = service.getProfile("John.Doe");

            assertThat(profile.username()).isEqualTo("John.Doe");
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void getProfile_notFound() {
            when(traineeRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProfile("unknown"))
                    .isInstanceOf(UserDoesNotExistException.class);
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("updates password when credentials match")
        void changePassword_success() {
            var trainee = buildTrainee();
            var request = new Request.ChangePassword("John.Doe", "secret", "newPass");

            when(traineeRepository.findByUsernameAndPassword("John.Doe", "secret"))
                    .thenReturn(Optional.of(trainee));
            when(traineeRepository.save(any(Trainee.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.changePassword(request);

            verify(traineeRepository).save(argThat(t -> "newPass".equals(t.getPassword())));
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when credentials do not match")
        void changePassword_wrongCredentials() {
            var request = new Request.ChangePassword("John.Doe", "wrong", "newPass");

            when(traineeRepository.findByUsernameAndPassword("John.Doe", "wrong"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.changePassword(request))
                    .isInstanceOf(UserDoesNotExistException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates and returns profile when trainee exists")
        void update_success() {
            var trainee = buildTrainee();

            var request = new Request.UpdateTrainee("John.Doe", "secret", "Jane", "Doe",
                    LocalDate.of(1991, 2, 2), new Address("456 New Rd", "Boston", "USA", "02101"));

            when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Profile.Trainee profile = service.update(request);

            assertThat(profile.firstName()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void update_notFound() {
            var request = new Request.UpdateTrainee("ghost", "secret", "Jane", "Doe",
                    LocalDate.of(1991, 2, 2), new Address("456 New Rd", "Boston", "USA", "02101"));

            when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(request))
                    .isInstanceOf(UserDoesNotExistException.class);
        }
    }

    @Nested
    @DisplayName("activate()")
    class Activate {

        @Test
        @DisplayName("activates trainee successfully")
        void activate_success() {
            when(traineeRepository.activateByUsername("John.Doe")).thenReturn(1);

            service.activate(new Request.ToggleActive("John.Doe", "secret"));

            verify(traineeRepository).activateByUsername("John.Doe");
        }

        @Test
        @DisplayName("throws AlreadyActivatedException when already active")
        void activate_alreadyActive() {
            when(traineeRepository.activateByUsername("John.Doe")).thenReturn(0);

            assertThatThrownBy(() -> service.activate(new Request.ToggleActive("John.Doe", "secret")))
                    .isInstanceOf(AlreadyActivatedException.class);
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class Deactivate {

        @Test
        @DisplayName("deactivates trainee successfully")
        void deactivate_success() {
            when(traineeRepository.deactivateByUsername("John.Doe")).thenReturn(1);

            service.deactivate(new Request.ToggleActive("John.Doe", "secret"));

            verify(traineeRepository).deactivateByUsername("John.Doe");
        }

        @Test
        @DisplayName("throws AlreadyDeactivatedException when already inactive")
        void deactivate_alreadyInactive() {
            when(traineeRepository.deactivateByUsername("John.Doe")).thenReturn(0);

            assertThatThrownBy(() -> service.deactivate(new Request.ToggleActive("John.Doe", "secret")))
                    .isInstanceOf(AlreadyDeactivatedException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("delegates to repository delete by username")
        void delete_success() {
            service.delete(new Request.DeleteTrainee("John.Doe", "secret"));

            verify(traineeRepository).deleteByUsername("John.Doe");
        }
    }

    @Nested
    @DisplayName("getTrainings()")
    class GetTrainings {

        @Test
        @DisplayName("returns trainings list from repository")
        void getTrainings_success() {
            var summary = mock(Summary.Training.class);

            when(traineeRepository.findTrainingsByUsername("John.Doe", null, null, null, null))
                    .thenReturn(List.of(summary));

            List<Summary.Training> result = service.getTrainings("John.Doe", null, null, null, null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("passes all filter parameters to repository")
        void getTrainings_withFilters() {
            var from = LocalDate.of(2024, 1, 1);
            var to = LocalDate.of(2024, 12, 31);

            when(traineeRepository.findTrainingsByUsername("John.Doe", from, to, "trainer1", "Yoga"))
                    .thenReturn(List.of());

            service.getTrainings("John.Doe", from, to, "trainer1", "Yoga");

            verify(traineeRepository).findTrainingsByUsername("John.Doe", from, to, "trainer1", "Yoga");
        }
    }

    @Nested
    @DisplayName("getUnassignedTrainers()")
    class GetUnassignedTrainers {

        @Test
        @DisplayName("returns trainers not in trainee's current list")
        void getUnassignedTrainers_success() {
            var trainer = mock(Trainer.class);
            when(trainer.getId()).thenReturn(1L);

            var trainee = buildTrainee();
            trainee.getTrainers().add(trainer);

            when(traineeRepository.findWTrainersByUsername("John.Doe"))
                    .thenReturn(Optional.of(trainee));
            when(trainerRepository.findAllByIdNotIn(anySet()))
                    .thenReturn(List.of());

            List<Profile.Trainer> result = service.getUnassignedTrainers("John.Doe");

            assertThat(result).isEmpty();
            verify(trainerRepository).findAllByIdNotIn(Set.of(1L));
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void getUnassignedTrainers_notFound() {
            when(traineeRepository.findWTrainersByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getUnassignedTrainers("ghost"))
                    .isInstanceOf(UserDoesNotExistException.class);
        }
    }

    @Nested
    @DisplayName("updateTrainers()")
    class UpdateTrainers {

        @Test
        @DisplayName("replaces trainer list and returns updated profiles")
        void updateTrainers_success() {
            var trainee = buildTrainee();
            var trainer = mock(Trainer.class);
            var trainingType = mock(TrainingType.class);

            when(trainer.getUsername())
                    .thenReturn("trainer1");
            when(trainer.getFirstName())
                    .thenReturn("Jane");
            when(trainer.getLastName())
                    .thenReturn("Smith");
            when(trainer.getSpecialization())
                    .thenReturn(trainingType);
            when(trainingType.getTrainingTypeName())
                    .thenReturn("Yoga");

            var request = new Request.UpdateTraineeTrainers("John.Doe", "secret", List.of("trainer1"));

            when(traineeRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(trainee));
            when(trainerRepository.findAllByUsernameIn(List.of("trainer1")))
                    .thenReturn(List.of(trainer));
            when(traineeRepository.save(any(Trainee.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            List<Profile.Trainer> result = service.updateTrainers(request);

            assertThat(result).hasSize(1);
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when one or more trainer usernames are invalid")
        void updateTrainers_missingTrainer() {
            var trainee = buildTrainee();
            var request = new Request.UpdateTraineeTrainers("John.Doe", "secret",
                    List.of("trainer1", "trainer2"));

            when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
            // Only one trainer found instead of two
            when(trainerRepository.findAllByUsernameIn(anyList())).thenReturn(List.of(mock(Trainer.class)));

            assertThatThrownBy(() -> service.updateTrainers(request))
                    .isInstanceOf(UserDoesNotExistException.class);
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void updateTrainers_traineeNotFound() {
            when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateTrainers(
                    new Request.UpdateTraineeTrainers("ghost", "secret", List.of())))
                    .isInstanceOf(UserDoesNotExistException.class);
        }
    }
}
