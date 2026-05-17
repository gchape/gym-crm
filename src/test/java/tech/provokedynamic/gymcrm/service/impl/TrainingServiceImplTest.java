package tech.provokedynamic.gymcrm.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.provokedynamic.gymcrm.dao.TraineeDao;
import tech.provokedynamic.gymcrm.dao.TrainerDao;
import tech.provokedynamic.gymcrm.dao.TrainingDao;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(1);

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Request.AddTraining validRequest() {
        return new Request.AddTraining(
                "alice.smith", "pass",
                "bob.jones",
                "Morning Yoga", "YOGA",
                FUTURE_DATE, 60
        );
    }

    @Test
    void add_persistsTraining_whenAllEntitiesFound() {
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
        when(trainerDao.findByUsername("bob.jones")).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName("YOGA")).thenReturn(yoga);

        trainingService.add(validRequest());

        verify(trainingDao).save(any());
    }

    @Test
    void add_throwsUserDoesNotExistException_whenTraineeNotFound() {
        when(traineeDao.findByUsername("alice.smith")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.add(validRequest()))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void add_throwsUserDoesNotExistException_whenTrainerNotFound() {
        var trainee = Trainee.builder()
                .firstName("Alice").lastName("Smith")
                .username("alice.smith").password("pass")
                .build();

        when(traineeDao.findByUsername("alice.smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername("bob.jones")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.add(validRequest()))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    void add_passesCorrectFieldsToTraining() {
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
        when(trainerDao.findByUsername("bob.jones")).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName("YOGA")).thenReturn(yoga);

        trainingService.add(validRequest());

        verify(trainingDao).save(argThat(t ->
                t.getTrainingName().equals("Morning Yoga") &&
                        t.getTrainingDate().equals(FUTURE_DATE) &&
                        t.getTrainingDuration() == 60 &&
                        t.getTrainee().equals(trainee) &&
                        t.getTrainer().equals(trainer)
        ));
    }
}
