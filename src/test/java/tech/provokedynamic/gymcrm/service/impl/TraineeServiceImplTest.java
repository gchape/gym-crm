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
import tech.provokedynamic.gymcrm.dto.Response;
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
import tech.provokedynamic.gymcrm.util.CredentialGenerator;  // unified interface

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeServiceImpl")
class TraineeServiceImplTest {

    @Mock
    TraineeRepository traineeRepository;

    @Mock
    TrainerRepository trainerRepository;

    @Mock
    CredentialGenerator credentialGenerator;  // same interface used by TrainerServiceImpl

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
        @DisplayName("generates credentials, persists trainee, and returns credentials")
        void create_success() {
            var request = new Request.CreateTrainee(
                    "John", "Doe",
                    LocalDate.of(1990, 1, 1),
                    new Address("123 Main St", "New York", "USA", "10001"));

            when(credentialGenerator.generateUsername("John", "Doe")).thenReturn("John.Doe");
            when(credentialGenerator.generatePassword()).thenReturn("pass123456");
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Response.CreatedUser credentials = service.create(request);

            assertThat(credentials.username()).isEqualTo("John.Doe");
            assertThat(credentials.password()).isEqualTo("pass123456");
            verify(traineeRepository).save(argThat(t ->
                    "John".equals(t.getFirstName())
                            && "Doe".equals(t.getLastName())
                            && "John.Doe".equals(t.getUsername())
                            && "pass123456".equals(t.getPassword())
            ));
        }

        @Test
        @DisplayName("passes optional fields (dateOfBirth, address) to persisted entity")
        void create_withOptionalFields() {
            var address = new Address("1 Road", "City", "Country", "00000");
            var dob = LocalDate.of(1995, 5, 20);
            var request = new Request.CreateTrainee("Jane", "Smith", dob, address);

            when(credentialGenerator.generateUsername("Jane", "Smith")).thenReturn("Jane.Smith");
            when(credentialGenerator.generatePassword()).thenReturn("pass123456");
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            service.create(request);

            verify(traineeRepository).save(argThat(t ->
                    dob.equals(t.getDateOfBirth()) && address.equals(t.getAddress())
            ));
        }

        @Test
        @DisplayName("works when dateOfBirth and address are null")
        void create_nullOptionalFields() {
            var request = new Request.CreateTrainee("Jane", "Smith", null, null);

            when(credentialGenerator.generateUsername("Jane", "Smith")).thenReturn("Jane.Smith");
            when(credentialGenerator.generatePassword()).thenReturn("pass123456");
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Response.CreatedUser credentials = service.create(request);

            assertThat(credentials.username()).isEqualTo("Jane.Smith");
        }
    }

    @Nested
    @DisplayName("getProfile()")
    class GetProfile {

        @Test
        @DisplayName("returns profile when trainee exists")
        void getProfile_found() {
            when(traineeRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(buildTrainee()));

            Profile.Trainee profile = service.getProfile("John.Doe");

            assertThat(profile.username()).isEqualTo("John.Doe");
            assertThat(profile.firstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void getProfile_notFound() {
            when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProfile("ghost"))
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
            var request = new Request.ChangePassword("John.Doe", "secret", "newPass123");

            when(traineeRepository.findByUsernameAndPassword("John.Doe", "secret"))
                    .thenReturn(Optional.of(trainee));
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            service.changePassword(request);

            verify(traineeRepository).save(argThat(t -> "newPass123".equals(t.getPassword())));
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when credentials do not match")
        void changePassword_wrongCredentials() {
            when(traineeRepository.findByUsernameAndPassword("John.Doe", "wrong"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.changePassword(
                    new Request.ChangePassword("John.Doe", "wrong", "newPass123")))
                    .isInstanceOf(UserDoesNotExistException.class);

            verify(traineeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates fields and returns the saved profile")
        void update_success() {
            var trainee = buildTrainee();
            var request = new Request.UpdateTrainee(
                    "John.Doe", "secret", "Jane", "Doe",
                    LocalDate.of(1991, 2, 2),
                    new Address("456 New Rd", "Boston", "USA", "02101"),
                    true);

            when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
            // Return the argument so the profile is built from the mutated entity
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            Profile.Trainee profile = service.update(request);

            assertThat(profile.firstName()).isEqualTo("Jane");
            verify(traineeRepository).save(argThat(t -> "Jane".equals(t.getFirstName())));
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void update_notFound() {
            when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(new Request.UpdateTrainee(
                    "ghost", "secret", "Jane", "Doe", null, null, true)))
                    .isInstanceOf(UserDoesNotExistException.class);

            verify(traineeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("activate()")
    class Activate {

        @Test
        @DisplayName("activates trainee when currently inactive")
        void activate_success() {
            when(traineeRepository.activateByUsername("John.Doe")).thenReturn(1);

            service.activate(new Request.ToggleActive("John.Doe", "secret", true));

            verify(traineeRepository).activateByUsername("John.Doe");
        }

        @Test
        @DisplayName("throws AlreadyActivatedException when trainee is already active")
        void activate_alreadyActive() {
            when(traineeRepository.activateByUsername("John.Doe")).thenReturn(0);

            assertThatThrownBy(() ->
                    service.activate(new Request.ToggleActive("John.Doe", "secret", true)))
                    .isInstanceOf(AlreadyActivatedException.class);
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class Deactivate {

        @Test
        @DisplayName("deactivates trainee when currently active")
        void deactivate_success() {
            when(traineeRepository.deactivateByUsername("John.Doe")).thenReturn(1);

            service.deactivate(new Request.ToggleActive("John.Doe", "secret", false));

            verify(traineeRepository).deactivateByUsername("John.Doe");
        }

        @Test
        @DisplayName("throws AlreadyDeactivatedException when trainee is already inactive")
        void deactivate_alreadyInactive() {
            when(traineeRepository.deactivateByUsername("John.Doe")).thenReturn(0);

            assertThatThrownBy(() ->
                    service.deactivate(new Request.ToggleActive("John.Doe", "secret", false)))
                    .isInstanceOf(AlreadyDeactivatedException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("delegates deletion to repository by username")
        void delete_success() {
            // Authentication is handled by the @Authenticated AOP aspect,
            // not inside this method, so only the repository call is verified.
            service.delete(new Request.DeleteTrainee("John.Doe", "secret"));

            verify(traineeRepository).deleteByUsername("John.Doe");
        }

        @Test
        @DisplayName("does not call any other repository method on delete")
        void delete_onlyCallsDeleteByUsername() {
            service.delete(new Request.DeleteTrainee("John.Doe", "secret"));

            verify(traineeRepository).deleteByUsername("John.Doe");
            verifyNoMoreInteractions(traineeRepository);
            verifyNoInteractions(trainerRepository);
        }
    }

    @Nested
    @DisplayName("getTrainings()")
    class GetTrainings {

        @Test
        @DisplayName("returns all trainings when no filters supplied")
        void getTrainings_noFilters() {
            var summary = mock(Summary.Training.class);
            when(traineeRepository.findTrainingsByUsername("John.Doe", null, null, null, null))
                    .thenReturn(List.of(summary));

            List<Summary.Training> result = service.getTrainings("John.Doe", null, null, null, null);

            assertThat(result).hasSize(1).containsExactly(summary);
        }

        @Test
        @DisplayName("passes all filter parameters through to the repository")
        void getTrainings_withFilters() {
            var from = LocalDate.of(2024, 1, 1);
            var to = LocalDate.of(2024, 12, 31);
            when(traineeRepository.findTrainingsByUsername("John.Doe", from, to, "trainer1", "Yoga"))
                    .thenReturn(List.of());

            service.getTrainings("John.Doe", from, to, "trainer1", "Yoga");

            verify(traineeRepository).findTrainingsByUsername("John.Doe", from, to, "trainer1", "Yoga");
        }

        @Test
        @DisplayName("returns empty list when no trainings exist")
        void getTrainings_empty() {
            when(traineeRepository.findTrainingsByUsername(any(), any(), any(), any(), any()))
                    .thenReturn(List.of());

            assertThat(service.getTrainings("John.Doe", null, null, null, null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUnassignedTrainers()")
    class GetUnassignedTrainers {

        @Test
        @DisplayName("queries active trainers excluding IDs already assigned to the trainee")
        void getUnassignedTrainers_excludesAssigned() {
            var assignedTrainer = mock(Trainer.class);
            when(assignedTrainer.getId()).thenReturn(1L);

            var trainee = buildTrainee();
            trainee.getTrainers().add(assignedTrainer);

            when(traineeRepository.findWTrainersByUsername("John.Doe"))
                    .thenReturn(Optional.of(trainee));
            when(trainerRepository.findAllByIdNotIn(anySet())).thenReturn(List.of());

            List<Profile.Trainer> result = service.getUnassignedTrainers("John.Doe");

            assertThat(result).isEmpty();
            verify(trainerRepository).findAllByIdNotIn(Set.of(1L));
        }

        @Test
        @DisplayName("passes empty set to repository when trainee has no assigned trainers")
        void getUnassignedTrainers_noAssignedTrainers() {
            var trainee = buildTrainee(); // no trainers
            when(traineeRepository.findWTrainersByUsername("John.Doe"))
                    .thenReturn(Optional.of(trainee));

            var unassigned = mock(Trainer.class);
            var type = mock(TrainingType.class);
            when(unassigned.getSpecialization()).thenReturn(type);
            when(type.getTrainingTypeName()).thenReturn("Yoga");
            when(trainerRepository.findAllByIdNotIn(Set.of())).thenReturn(List.of(unassigned));

            List<Profile.Trainer> result = service.getUnassignedTrainers("John.Doe");

            assertThat(result).hasSize(1);
            verify(trainerRepository).findAllByIdNotIn(Set.of());
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void getUnassignedTrainers_notFound() {
            when(traineeRepository.findWTrainersByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getUnassignedTrainers("ghost"))
                    .isInstanceOf(UserDoesNotExistException.class);

            verifyNoInteractions(trainerRepository);
        }
    }

    @Nested
    @DisplayName("updateTrainers()")
    class UpdateTrainers {

        @Test
        @DisplayName("replaces trainer list with resolved trainers and returns updated profiles")
        void updateTrainers_success() {
            var trainee = buildTrainee();
            var trainer = mock(Trainer.class);
            var trainingType = mock(TrainingType.class);

            when(trainer.getUsername()).thenReturn("trainer1");
            when(trainer.getFirstName()).thenReturn("Jane");
            when(trainer.getLastName()).thenReturn("Smith");
            when(trainer.getSpecialization()).thenReturn(trainingType);
            when(trainingType.getTrainingTypeName()).thenReturn("Yoga");

            var request = new Request.UpdateTraineeTrainers("John.Doe", "secret", List.of("trainer1"));

            when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
            // findAllByUsernameIn signature accepts a Collection
            when(trainerRepository.findAllByUsernameIn(List.of("trainer1"))).thenReturn(List.of(trainer));
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

            List<Profile.Trainer> result = service.updateTrainers(request);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().username()).isEqualTo("trainer1");
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when fewer trainers resolved than requested")
        void updateTrainers_someTrainersNotFound() {
            var trainee = buildTrainee();
            var request = new Request.UpdateTraineeTrainers(
                    "John.Doe", "secret", List.of("trainer1", "trainer2"));

            when(traineeRepository.findByUsername("John.Doe")).thenReturn(Optional.of(trainee));
            // Only one of the two requested usernames resolves
            when(trainerRepository.findAllByUsernameIn(any())).thenReturn(List.of(mock(Trainer.class)));

            assertThatThrownBy(() -> service.updateTrainers(request))
                    .isInstanceOf(UserDoesNotExistException.class);

            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void updateTrainers_traineeNotFound() {
            when(traineeRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateTrainers(
                    new Request.UpdateTraineeTrainers("ghost", "secret", List.of("t1"))))
                    .isInstanceOf(UserDoesNotExistException.class);

            verifyNoInteractions(trainerRepository);
        }
    }
}