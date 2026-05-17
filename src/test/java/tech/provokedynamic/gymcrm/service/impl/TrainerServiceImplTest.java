package tech.provokedynamic.gymcrm.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private CredentialGenerator credentialGenerator;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    void create_savesTrainer_andReturnsProfile() {
        var yoga = new TrainingType("YOGA");
        var request = new Request.CreateTrainer("Bob", "Jones", "YOGA");

        when(credentialGenerator.generateUsername("Bob", "Jones")).thenReturn("Bob.Jones");
        when(credentialGenerator.generatePassword()).thenReturn("secretPass1");
        when(trainingTypeDao.findByName("YOGA")).thenReturn(yoga);

        var result = trainerService.create(request);

        verify(trainerDao).save(any(Trainer.class));
        assertThat(result.username()).isEqualTo("Bob.Jones");
        assertThat(result.firstName()).isEqualTo("Bob");
        assertThat(result.specialization()).isEqualTo("YOGA");
    }

    @Test
    void create_usesGeneratedCredentials() {
        when(credentialGenerator.generateUsername("Bob", "Jones")).thenReturn("Bob.Jones");
        when(credentialGenerator.generatePassword()).thenReturn("secretPass1");
        when(trainingTypeDao.findByName("YOGA")).thenReturn(new TrainingType("YOGA"));

        trainerService.create(new Request.CreateTrainer("Bob", "Jones", "YOGA"));

        verify(credentialGenerator).generateUsername("Bob", "Jones");
        verify(credentialGenerator).generatePassword();
    }

    @Test
    void getProfile_returnsProfile_whenTrainerExists() {
        var trainer = Trainer.builder()
                .firstName("Bob").lastName("Jones")
                .username("bob.jones").password("pass")
                .specialization(new TrainingType("YOGA"))
                .build();

        when(trainerDao.findByUsername("bob.jones")).thenReturn(Optional.of(trainer));

        var result = trainerService.getProfile("bob.jones");

        assertThat(result.username()).isEqualTo("bob.jones");
    }

    @Test
    void getProfile_throwsUserDoesNotExistException_whenNotFound() {
        when(trainerDao.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getProfile("ghost"))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void changePassword_delegatesToDao() {
        var request = new Request.ChangePassword("bob.jones", "oldPass1", "newPass1");

        trainerService.changePassword(request);

        verify(trainerDao).updatePassword("bob.jones", "newPass1");
    }

    @Test
    void update_returnsUpdatedProfile_whenTrainerExists() {
        var yoga = new TrainingType("YOGA");
        var pilates = new TrainingType("PILATES");

        var trainer = Trainer.builder()
                .firstName("Bob").lastName("Jones")
                .username("bob.jones").password("pass")
                .specialization(yoga)
                .build();

        when(trainerDao.findByUsername("bob.jones")).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName("PILATES")).thenReturn(pilates);

        var request = new Request.UpdateTrainer(
                "bob.jones", "pass", "Robert", "Jones", "PILATES");

        var result = trainerService.update(request);

        verify(trainerDao).update(any(Trainer.class));
        assertThat(result.firstName()).isEqualTo("Robert");
        assertThat(result.specialization()).isEqualTo("PILATES");
    }

    @Test
    void update_throwsUserDoesNotExistException_whenNotFound() {
        when(trainerDao.findByUsername("ghost")).thenReturn(Optional.empty());

        var request = new Request.UpdateTrainer(
                "ghost", "pass", "Ghost", "User", "YOGA");

        assertThatThrownBy(() -> trainerService.update(request))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void activate_succeeds_whenTrainerIsInactive() {
        when(trainerDao.activateByUsername("bob.jones")).thenReturn(1);

        trainerService.activate(new Request.ToggleActive("bob.jones", "pass"));

        verify(trainerDao).activateByUsername("bob.jones");
    }

    @Test
    void activate_throwsAlreadyActivatedException_whenAlreadyActive() {
        when(trainerDao.activateByUsername("bob.jones")).thenReturn(0);

        assertThatThrownBy(() -> trainerService.activate(
                new Request.ToggleActive("bob.jones", "pass")))
                .isInstanceOf(AlreadyActivatedException.class);
    }

    @Test
    void deactivate_succeeds_whenTrainerIsActive() {
        when(trainerDao.deactivateByUsername("bob.jones")).thenReturn(1);

        trainerService.deactivate(new Request.ToggleActive("bob.jones", "pass"));

        verify(trainerDao).deactivateByUsername("bob.jones");
    }

    @Test
    void deactivate_throwsAlreadyDeactivatedException_whenAlreadyInactive() {
        when(trainerDao.deactivateByUsername("bob.jones")).thenReturn(0);

        assertThatThrownBy(() -> trainerService.deactivate(
                new Request.ToggleActive("bob.jones", "pass")))
                .isInstanceOf(AlreadyDeactivatedException.class);
    }

    // --- getTrainings ---

    @Test
    void getTrainings_delegatesToDao_withAllFilters() {
        var from = LocalDate.of(2024, 1, 1);
        var to = LocalDate.of(2024, 6, 1);
        var expected = List.of(new Summary.Training("Yoga", from, 60, "alice.smith"));

        when(trainerDao.findTrainingsByUsername("bob.jones", from, to, "alice.smith"))
                .thenReturn(expected);

        var result = trainerService.getTrainings("bob.jones", from, to, "alice.smith");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getTrainings_delegatesToDao_withNullFilters() {
        when(trainerDao.findTrainingsByUsername("bob.jones", null, null, null))
                .thenReturn(List.of());

        var result = trainerService.getTrainings("bob.jones", null, null, null);

        assertThat(result).isEmpty();
    }
}
