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
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.TrainingTypeNotFoundException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerServiceImpl")
class TrainerServiceImplTest {

    @Mock
    TrainerRepository trainerRepository;

    @Mock
    TrainingTypeRepository trainingTypeRepository;

    @Mock
    CredentialGenerator credentialGenerator;

    @InjectMocks
    TrainerServiceImpl service;

    private Trainer buildTrainer() {
        var type = new TrainingType("Yoga");

        return Trainer.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("Alice.Smith")
                .password("secret")
                .specialization(type)
                .build();
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("generates credentials, resolves specialization, persists trainer, and returns profile")
        void create_success() {
            var request = new Request.CreateTrainer("Alice", "Smith", "Yoga");
            var type = new TrainingType("Yoga");

            when(credentialGenerator.generateUsername("Alice", "Smith"))
                    .thenReturn("Alice.Smith");
            when(credentialGenerator.generatePassword())
                    .thenReturn("pass123");
            when(trainingTypeRepository.findByTrainingTypeName("Yoga"))
                    .thenReturn(Optional.of(type));
            when(trainerRepository.save(any(Trainer.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Profile.Trainer profile = service.create(request);

            assertThat(profile.username()).isEqualTo("Alice.Smith");
            verify(trainerRepository).save(any(Trainer.class));
        }

        @Test
        @DisplayName("throws TrainingTypeNotFoundException when specialization not found")
        void create_unknownSpecialization() {
            var request = new Request.CreateTrainer("Alice", "Smith", "Unknown");

            when(credentialGenerator.generateUsername(any(), any()))
                    .thenReturn("Alice.Smith");
            when(credentialGenerator.generatePassword())
                    .thenReturn("pass");
            when(trainingTypeRepository.findByTrainingTypeName("Unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(TrainingTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getProfile()")
    class GetProfile {

        @Test
        @DisplayName("returns profile when trainer exists")
        void getProfile_found() {
            when(trainerRepository.findByUsername("Alice.Smith"))
                    .thenReturn(Optional.of(buildTrainer()));

            Profile.Trainer profile = service.getProfile("Alice.Smith");

            assertThat(profile.username()).isEqualTo("Alice.Smith");
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainer not found")
        void getProfile_notFound() {
            when(trainerRepository.findByUsername("ghost"))
                    .thenReturn(Optional.empty());

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
            var trainer = buildTrainer();
            var request = new Request.ChangePassword("Alice.Smith", "secret", "newPass");

            when(trainerRepository.findByUsernameAndPassword("Alice.Smith", "secret"))
                    .thenReturn(Optional.of(trainer));
            when(trainerRepository.save(any(Trainer.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.changePassword(request);

            verify(trainerRepository).save(argThat(t -> "newPass".equals(t.getPassword())));
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when credentials do not match")
        void changePassword_wrongCredentials() {
            var request = new Request.ChangePassword("Alice.Smith", "wrong", "newPass");

            when(trainerRepository.findByUsernameAndPassword("Alice.Smith", "wrong"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.changePassword(request))
                    .isInstanceOf(UserDoesNotExistException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates and returns profile when trainer and specialization exist")
        void update_success() {
            var trainer = buildTrainer();
            var type = new TrainingType("Pilates");
            var request = new Request.UpdateTrainer("Alice.Smith", "aliceSmith", "Alice", "Jones", "Pilates");

            when(trainerRepository.findByUsername("Alice.Smith"))
                    .thenReturn(Optional.of(trainer));
            when(trainingTypeRepository.findByTrainingTypeName("Pilates"))
                    .thenReturn(Optional.of(type));
            when(trainerRepository.save(any(Trainer.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Profile.Trainer profile = service.update(request);

            assertThat(profile.lastName()).isEqualTo("Jones");
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainer not found")
        void update_trainerNotFound() {
            when(trainerRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.update(new Request.UpdateTrainer("ghost", "A", "B", "Yoga", "")))
                    .isInstanceOf(UserDoesNotExistException.class);
        }

        @Test
        @DisplayName("throws TrainingTypeNotFoundException when specialization not found")
        void update_specializationNotFound() {
            when(trainerRepository.findByUsername("Alice.Smith"))
                    .thenReturn(Optional.of(buildTrainer()));

            when(trainingTypeRepository.findByTrainingTypeName("Unknown"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    service.update(new Request.UpdateTrainer("Alice.Smith", "aliceSmith", "A", "B", "Unknown")))
                    .isInstanceOf(TrainingTypeNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("activate()")
    class Activate {

        @Test
        @DisplayName("activates trainer successfully")
        void activate_success() {
            when(trainerRepository.activateByUsername("Alice.Smith")).thenReturn(1);

            service.activate(new Request.ToggleActive("Alice.Smith", "secret"));

            verify(trainerRepository).activateByUsername("Alice.Smith");
        }

        @Test
        @DisplayName("throws AlreadyActivatedException when already active")
        void activate_alreadyActive() {
            when(trainerRepository.activateByUsername("Alice.Smith")).thenReturn(0);

            assertThatThrownBy(() -> service.activate(new Request.ToggleActive("Alice.Smith", "secret")))
                    .isInstanceOf(AlreadyActivatedException.class);
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class Deactivate {

        @Test
        @DisplayName("deactivates trainer successfully")
        void deactivate_success() {
            when(trainerRepository.deactivateByUsername("Alice.Smith")).thenReturn(1);

            service.deactivate(new Request.ToggleActive("Alice.Smith", "secret"));

            verify(trainerRepository).deactivateByUsername("Alice.Smith");
        }

        @Test
        @DisplayName("throws AlreadyDeactivatedException when already inactive")
        void deactivate_alreadyInactive() {
            when(trainerRepository.deactivateByUsername("Alice.Smith")).thenReturn(0);

            assertThatThrownBy(() -> service.deactivate(new Request.ToggleActive("Alice.Smith", "secret")))
                    .isInstanceOf(AlreadyDeactivatedException.class);
        }
    }

    @Nested
    @DisplayName("getTrainings()")
    class GetTrainings {

        @Test
        @DisplayName("returns trainings list from repository")
        void getTrainings_success() {
            var summary = mock(Summary.Training.class);

            when(trainerRepository.findTrainingsByUsername("Alice.Smith", null, null, null))
                    .thenReturn(List.of(summary));

            List<Summary.Training> result = service.getTrainings("Alice.Smith", null, null, null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("passes all filter parameters to repository")
        void getTrainings_withFilters() {
            var from = LocalDate.of(2024, 1, 1);
            var to = LocalDate.of(2024, 12, 31);

            when(trainerRepository.findTrainingsByUsername("Alice.Smith", from, to, "trainee1"))
                    .thenReturn(List.of());

            service.getTrainings("Alice.Smith", from, to, "trainee1");

            verify(trainerRepository).findTrainingsByUsername("Alice.Smith", from, to, "trainee1");
        }
    }
}
