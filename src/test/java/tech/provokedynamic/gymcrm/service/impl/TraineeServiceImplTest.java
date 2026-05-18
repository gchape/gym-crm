package tech.provokedynamic.gymcrm.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dto.Profile;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.AlreadyActivatedException;
import tech.provokedynamic.gymcrm.exception.AlreadyDeactivatedException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.util.CredentialGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private CredentialGenerator credentialGenerator;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Test
    void create_savesTrainee_andReturnsProfile() {
        var request = new Request.CreateTrainee("Alice", "Smith", null, null);

        when(credentialGenerator.generateUsername("Alice", "Smith")).thenReturn("Alice.Smith");
        when(credentialGenerator.generatePassword()).thenReturn("secretPass1");

        var result = traineeService.create(request);

        verify(traineeDao).save(any(Trainee.class));
        assertThat(result.username()).isEqualTo("Alice.Smith");
        assertThat(result.firstName()).isEqualTo("Alice");
        assertThat(result.lastName()).isEqualTo("Smith");
    }

    @Test
    void create_usesGeneratedCredentials() {
        var request = new Request.CreateTrainee("Alice", "Smith", null, null);

        when(credentialGenerator.generateUsername("Alice", "Smith")).thenReturn("Alice.Smith");
        when(credentialGenerator.generatePassword()).thenReturn("secretPass1");

        traineeService.create(request);

        verify(credentialGenerator).generateUsername("Alice", "Smith");
        verify(credentialGenerator).generatePassword();
    }

    @Test
    void getProfile_returnsProfile_whenTraineeExists() {
        var trainee = Trainee.builder()
                .firstName("Alice").lastName("Smith")
                .username("alice.smith").password("pass")
                .build();

        when(traineeDao.findByUsername("alice.smith")).thenReturn(Optional.of(trainee));

        var result = traineeService.getProfile("alice.smith");

        assertThat(result.username()).isEqualTo("alice.smith");
    }

    @Test
    void getProfile_throwsUserDoesNotExistException_whenNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getProfile("ghost"))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void changePassword_delegatesToDao() {
        var request = new Request.ChangePassword("alice.smith", "oldPass1", "newPass1");

        traineeService.changePassword(request);

        verify(traineeDao).updatePassword("alice.smith", "newPass1");
    }

    @Test
    void update_returnsUpdatedProfile_whenTraineeExists() {
        var trainee = Trainee.builder()
                .firstName("Alice").lastName("Smith")
                .username("alice.smith").password("pass")
                .build();

        when(traineeDao.findByUsername("alice.smith")).thenReturn(Optional.of(trainee));

        var request = new Request.UpdateTrainee(
                "alice.smith", "pass", "Alicia", "Smith", null, null);

        var result = traineeService.update(request);

        verify(traineeDao).update(any(Trainee.class));
        assertThat(result.firstName()).isEqualTo("Alicia");
    }

    @Test
    void update_throwsUserDoesNotExistException_whenNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());

        var request = new Request.UpdateTrainee(
                "ghost", "pass", "Ghost", "User", null, null);

        assertThatThrownBy(() -> traineeService.update(request))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void activate_succeeds_whenTraineeIsInactive() {
        when(traineeDao.activateByUsername("alice.smith")).thenReturn(1);

        traineeService.activate(new Request.ToggleActive("alice.smith", "pass"));

        verify(traineeDao).activateByUsername("alice.smith");
    }

    @Test
    void activate_throwsAlreadyActivatedException_whenAlreadyActive() {
        when(traineeDao.activateByUsername("alice.smith")).thenReturn(0);

        assertThatThrownBy(() -> traineeService.activate(
                new Request.ToggleActive("alice.smith", "pass")))
                .isInstanceOf(AlreadyActivatedException.class);
    }

    @Test
    void deactivate_succeeds_whenTraineeIsActive() {
        when(traineeDao.deactivateByUsername("alice.smith")).thenReturn(1);

        traineeService.deactivate(new Request.ToggleActive("alice.smith", "pass"));

        verify(traineeDao).deactivateByUsername("alice.smith");
    }

    @Test
    void deactivate_throwsAlreadyDeactivatedException_whenAlreadyInactive() {
        when(traineeDao.deactivateByUsername("alice.smith")).thenReturn(0);

        assertThatThrownBy(() -> traineeService.deactivate(
                new Request.ToggleActive("alice.smith", "pass")))
                .isInstanceOf(AlreadyDeactivatedException.class);
    }

    @Test
    void delete_removesTrainee_whenFound() {
        var trainee = Trainee.builder()
                .firstName("Alice").lastName("Smith")
                .username("alice.smith").password("pass")
                .build();

        when(traineeDao.findByUsername("alice.smith")).thenReturn(Optional.of(trainee));

        traineeService.delete(new Request.DeleteTrainee("alice.smith", "pass"));

        verify(traineeDao).delete(trainee);
    }

    @Test
    void delete_throwsUserDoesNotExistException_whenNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.delete(
                new Request.DeleteTrainee("ghost", "pass")))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void getTrainings_delegatesToDao_withAllFilters() {
        var from = LocalDate.of(2024, 1, 1);
        var to = LocalDate.of(2024, 6, 1);
        var expected = List.of(new Summary.Training("Yoga", from, 60, "bob.jones"));

        when(traineeDao.findTrainingsByUsername("alice.smith", from, to, "bob.jones", "YOGA"))
                .thenReturn(expected);

        var result = traineeService.getTrainings("alice.smith", from, to, "bob.jones", "YOGA");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getTrainings_delegatesToDao_withNullFilters() {
        when(traineeDao.findTrainingsByUsername("alice.smith", null, null, null, null))
                .thenReturn(List.of());

        var result = traineeService.getTrainings("alice.smith", null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getUnassignedTrainers_returnsResultFromDao() {
        var trainer = new Profile.Trainer("Bob", "Jones", "bob.jones", "YOGA");
        when(traineeDao.findUnassignedTrainers("alice.smith")).thenReturn(List.of(trainer));

        var result = traineeService.getUnassignedTrainers("alice.smith");

        assertThat(result).containsExactly(trainer);
    }

    @Test
    void updateTrainers_replacesTrainers_andReturnsAssigned() {
        var trainee = Trainee.builder()
                .firstName("Alice").lastName("Smith")
                .username("alice.smith").password("pass")
                .build();

        var yoga = new TrainingType("YOGA");
        var trainer = Trainer.builder()
                .firstName("Bob").lastName("Jones")
                .username("bob.jones").password("pass")
                .specialization(yoga)
                .build();

        var assigned = List.of(new Profile.Trainer("Bob", "Jones", "bob.jones", "YOGA"));

        when(traineeDao.findByUsername("alice.smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsernames(Set.of("bob.jones"))).thenReturn(List.of(trainer));
        when(traineeDao.findAssignedTrainers("alice.smith")).thenReturn(assigned);

        var request = new Request.UpdateTraineeTrainers(
                "alice.smith", "pass", List.of("bob.jones"));

        var result = traineeService.updateTrainers(request);

        verify(traineeDao).update(trainee);
        assertThat(result).isEqualTo(assigned);
    }

    @Test
    void updateTrainers_throwsUserDoesNotExistException_whenTraineeNotFound() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());

        var request = new Request.UpdateTraineeTrainers(
                "ghost", "pass", List.of("bob.jones"));

        assertThatThrownBy(() -> traineeService.updateTrainers(request))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void updateTrainers_throwsUserDoesNotExistException_whenSomeTrainersNotFound() {
        var trainee = Trainee.builder()
                .firstName("Alice").lastName("Smith")
                .username("alice.smith").password("pass")
                .build();

        when(traineeDao.findByUsername("alice.smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsernames(Set.of("bob.jones", "ghost"))).thenReturn(List.of());

        var request = new Request.UpdateTraineeTrainers(
                "alice.smith", "pass", List.of("bob.jones", "ghost"));

        assertThatThrownBy(() -> traineeService.updateTrainers(request))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void updateTrainers_deduplicatesRequestedUsernames() {
        var trainee = Trainee.builder()
                .firstName("Alice").lastName("Smith")
                .username("alice.smith").password("pass")
                .build();

        var yoga = new TrainingType("YOGA");
        var trainer = Trainer.builder()
                .firstName("Bob").lastName("Jones")
                .username("bob.jones").password("pass")
                .specialization(yoga)
                .build();

        when(traineeDao.findByUsername("alice.smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsernames(Set.of("bob.jones"))).thenReturn(List.of(trainer));
        when(traineeDao.findAssignedTrainers("alice.smith")).thenReturn(List.of());

        var request = new Request.UpdateTraineeTrainers(
                "alice.smith", "pass", List.of("bob.jones", "bob.jones"));

        traineeService.updateTrainers(request);

        verify(trainerDao).findByUsernames(Set.of("bob.jones"));
    }
}
