package tech.provokedynamic.gymcrm.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Response;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.*;
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
    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    TrainerServiceImpl service;

    private TrainingType yogaType() {
        return new TrainingType("Yoga");
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .firstName("Alice")
                .lastName("Smith")
                .username("Alice.Smith")
                .password("$2y$encoded")
                .specialization(yogaType())
                .build();
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("generates credentials, encodes password, persists trainer, returns raw password")
        void create_success() {
            var type = yogaType();
            when(credentialGenerator.generateUsername("Alice", "Smith")).thenReturn("Alice.Smith");
            when(credentialGenerator.generatePassword()).thenReturn("pass123456");
            when(passwordEncoder.encode("pass123456")).thenReturn("$2y$encoded");
            when(trainingTypeRepository.findByTrainingTypeName("Yoga")).thenReturn(Optional.of(type));
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

            Response.CreatedUser credentials = service.create(new Request.CreateTrainer("Alice", "Smith", "Yoga"));

            assertThat(credentials.username()).isEqualTo("Alice.Smith");
            assertThat(credentials.password()).isEqualTo("pass123456");
            verify(trainerRepository).save(argThat(t -> "$2y$encoded".equals(t.getPassword())));
        }

        @Test
        @DisplayName("returned password is raw, not the encoded one")
        void create_returnsRawPassword() {
            when(credentialGenerator.generateUsername(any(), any())).thenReturn("Alice.Smith");
            when(credentialGenerator.generatePassword()).thenReturn("rawPass123");
            when(passwordEncoder.encode("rawPass123")).thenReturn("$2y$hashed");
            when(trainingTypeRepository.findByTrainingTypeName(any())).thenReturn(Optional.of(yogaType()));
            when(trainerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var creds = service.create(new Request.CreateTrainer("Alice", "Smith", "Yoga"));

            assertThat(creds.password()).isEqualTo("rawPass123");
        }

        @Test
        @DisplayName("throws TrainingTypeNotFoundException when specialization does not exist")
        void create_unknownSpecialization() {
            when(credentialGenerator.generateUsername(any(), any())).thenReturn("Alice.Smith");
            when(credentialGenerator.generatePassword()).thenReturn("pass123456");
            when(trainingTypeRepository.findByTrainingTypeName("Unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(new Request.CreateTrainer("Alice", "Smith", "Unknown")))
                    .isInstanceOf(TrainingTypeNotFoundException.class);

            verify(trainerRepository, never()).save(any());
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
            assertThat(profile.specialization()).isEqualTo("Yoga");
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainer not found")
        void getProfile_notFound() {
            when(trainerRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getProfile("ghost"))
                    .isInstanceOf(UserDoesNotExistException.class);
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("encodes and saves new password when current password matches")
        void changePassword_success() {
            var trainer = buildTrainer();
            when(trainerRepository.findByUsername("Alice.Smith")).thenReturn(Optional.of(trainer));
            when(passwordEncoder.matches("secret", "$2y$encoded")).thenReturn(true);
            when(passwordEncoder.encode("newPass123")).thenReturn("$2y$newEncoded");
            when(trainerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.changePassword(new Request.ChangePassword("Alice.Smith", "secret", "newPass123"));

            verify(trainerRepository).save(argThat(t -> "$2y$newEncoded".equals(t.getPassword())));
        }

        @Test
        @DisplayName("throws AuthenticationException when current password does not match")
        void changePassword_wrongPassword() {
            var trainer = buildTrainer();
            when(trainerRepository.findByUsername("Alice.Smith")).thenReturn(Optional.of(trainer));
            when(passwordEncoder.matches("wrong", "$2y$encoded")).thenReturn(false);

            assertThatThrownBy(() -> service.changePassword(
                    new Request.ChangePassword("Alice.Smith", "wrong", "newPass123")))
                    .isInstanceOf(AuthenticationException.class);

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainer not found")
        void changePassword_userNotFound() {
            when(trainerRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.changePassword(
                    new Request.ChangePassword("ghost", "pass", "newPass123")))
                    .isInstanceOf(UserDoesNotExistException.class);
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates fields and returns the saved profile")
        void update_success() {
            var trainer = buildTrainer();
            var pilates = new TrainingType("Pilates");
            var request = new Request.UpdateTrainer("Alice.Smith", "Alice", "Jones", "Pilates", true);

            when(trainerRepository.findByUsername("Alice.Smith")).thenReturn(Optional.of(trainer));
            when(trainingTypeRepository.findByTrainingTypeName("Pilates")).thenReturn(Optional.of(pilates));
            when(trainerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Profile.Trainer profile = service.update(request);

            assertThat(profile.lastName()).isEqualTo("Jones");
            assertThat(profile.specialization()).isEqualTo("Pilates");
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainer not found")
        void update_trainerNotFound() {
            when(trainerRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(
                    new Request.UpdateTrainer("ghost", "A", "B", "Yoga", true)))
                    .isInstanceOf(UserDoesNotExistException.class);

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws TrainingTypeNotFoundException when new specialization does not exist")
        void update_specializationNotFound() {
            when(trainerRepository.findByUsername("Alice.Smith")).thenReturn(Optional.of(buildTrainer()));
            when(trainingTypeRepository.findByTrainingTypeName("Unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(
                    new Request.UpdateTrainer("Alice.Smith", "A", "B", "Unknown", true)))
                    .isInstanceOf(TrainingTypeNotFoundException.class);

            verify(trainerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("activate()")
    class Activate {

        @Test
        @DisplayName("activates trainer when currently inactive")
        void activate_success() {
            when(trainerRepository.activateByUsername("Alice.Smith")).thenReturn(1);

            service.activate(new Request.ToggleActive("Alice.Smith", true));

            verify(trainerRepository).activateByUsername("Alice.Smith");
        }

        @Test
        @DisplayName("throws AlreadyActivatedException when trainer is already active")
        void activate_alreadyActive() {
            when(trainerRepository.activateByUsername("Alice.Smith")).thenReturn(0);

            assertThatThrownBy(() -> service.activate(new Request.ToggleActive("Alice.Smith", true)))
                    .isInstanceOf(AlreadyActivatedException.class);
        }
    }

    @Nested
    @DisplayName("deactivate()")
    class Deactivate {

        @Test
        @DisplayName("deactivates trainer when currently active")
        void deactivate_success() {
            when(trainerRepository.deactivateByUsername("Alice.Smith")).thenReturn(1);

            service.deactivate(new Request.ToggleActive("Alice.Smith", false));

            verify(trainerRepository).deactivateByUsername("Alice.Smith");
        }

        @Test
        @DisplayName("throws AlreadyDeactivatedException when trainer is already inactive")
        void deactivate_alreadyInactive() {
            when(trainerRepository.deactivateByUsername("Alice.Smith")).thenReturn(0);

            assertThatThrownBy(() -> service.deactivate(new Request.ToggleActive("Alice.Smith", false)))
                    .isInstanceOf(AlreadyDeactivatedException.class);
        }
    }

    @Nested
    @DisplayName("getTrainings()")
    class GetTrainings {

        @Test
        @DisplayName("returns all trainings when no filters supplied")
        void getTrainings_noFilters() {
            var summary = mock(Summary.Training.class);
            when(trainerRepository.findTrainingsByUsername("Alice.Smith", null, null, null))
                    .thenReturn(List.of(summary));

            List<Summary.Training> result = service.getTrainings("Alice.Smith", null, null, null);

            assertThat(result).hasSize(1).containsExactly(summary);
        }

        @Test
        @DisplayName("passes all filter parameters through to the repository")
        void getTrainings_withFilters() {
            var from = LocalDate.of(2024, 1, 1);
            var to = LocalDate.of(2024, 12, 31);
            when(trainerRepository.findTrainingsByUsername("Alice.Smith", from, to, "trainee1"))
                    .thenReturn(List.of());

            service.getTrainings("Alice.Smith", from, to, "trainee1");

            verify(trainerRepository).findTrainingsByUsername("Alice.Smith", from, to, "trainee1");
        }

        @Test
        @DisplayName("returns empty list when no trainings exist")
        void getTrainings_empty() {
            when(trainerRepository.findTrainingsByUsername(any(), any(), any(), any()))
                    .thenReturn(List.of());

            assertThat(service.getTrainings("Alice.Smith", null, null, null)).isEmpty();
        }
    }
}
