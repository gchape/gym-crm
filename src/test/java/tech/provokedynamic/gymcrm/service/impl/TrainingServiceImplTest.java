package tech.provokedynamic.gymcrm.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingServiceImpl")
class TrainingServiceImplTest {

    @Mock
    TrainingRepository trainingRepository;

    @Mock
    TraineeRepository traineeRepository;

    @Mock
    TrainerRepository trainerRepository;

    @InjectMocks
    TrainingServiceImpl service;

    private Request.AddTraining validRequest() {
        return new Request.AddTraining(
                "trainee1",
                "pass123456",   // traineePassword
                "trainer1",
                "Morning Yoga",
                LocalDate.of(2025, 8, 1),  // future-or-present
                60
        );
    }

    @Nested
    @DisplayName("add()")
    class Add {

        @Test
        @DisplayName("persists training when trainee and trainer both exist")
        void add_success() {
            var trainee = mock(Trainee.class);
            var trainer = mock(Trainer.class);
            var type = mock(TrainingType.class);
            when(trainer.getSpecialization()).thenReturn(type);

            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
            when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));
            when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            service.add(validRequest());

            verify(trainingRepository).save(argThat(t ->
                    t.getTrainee() == trainee
                            && t.getTrainer() == trainer
                            && t.getTrainingType() == type
                            && "Morning Yoga".equals(t.getTrainingName())
                            && LocalDate.of(2025, 8, 1).equals(t.getTrainingDate())
                            && t.getTrainingDuration() == 60
            ));
        }

        @Test
        @DisplayName("uses trainer's specialization as the training type — no separate type lookup")
        void add_trainingTypeComesFromTrainerSpecialization() {
            var trainee = mock(Trainee.class);
            var trainer = mock(Trainer.class);
            var yoga = mock(TrainingType.class);
            when(trainer.getSpecialization()).thenReturn(yoga);

            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
            when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));
            when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            service.add(validRequest());

            // specialization must be read exactly once to populate the saved entity
            verify(trainer, times(1)).getSpecialization();
            verify(trainingRepository).save(argThat(t -> t.getTrainingType() == yoga));
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void add_traineeNotFound() {
            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.add(validRequest()))
                    .isInstanceOf(UserDoesNotExistException.class);

            verifyNoInteractions(trainerRepository, trainingRepository);
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainer not found")
        void add_trainerNotFound() {
            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(mock(Trainee.class)));
            when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.add(validRequest()))
                    .isInstanceOf(UserDoesNotExistException.class);

            verifyNoInteractions(trainingRepository);
        }

        @Test
        @DisplayName("does not save anything when trainee lookup fails")
        void add_noSaveWhenTraineeMissing() {
            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.add(validRequest()))
                    .isInstanceOf(UserDoesNotExistException.class);

            verify(trainingRepository, never()).save(any());
        }

        @Test
        @DisplayName("does not save anything when trainer lookup fails")
        void add_noSaveWhenTrainerMissing() {
            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(mock(Trainee.class)));
            when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.add(validRequest()))
                    .isInstanceOf(UserDoesNotExistException.class);

            verify(trainingRepository, never()).save(any());
        }
    }
}
