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
import tech.provokedynamic.gymcrm.exception.TrainingTypeNotFoundException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.TraineeRepository;
import tech.provokedynamic.gymcrm.repository.TrainerRepository;
import tech.provokedynamic.gymcrm.repository.TrainingRepository;
import tech.provokedynamic.gymcrm.repository.TrainingTypeRepository;

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

    @Mock
    TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    TrainingServiceImpl service;

    private Request.AddTraining validRequest() {
        return new Request.AddTraining(
                "trainee1",
                "trainer111",
                "trainer1",
                "Morning Yoga",
                "Yoga",
                LocalDate.of(2024, 6, 1),
                60
        );
    }

    @Nested
    @DisplayName("add()")
    class Add {

        @Test
        @DisplayName("persists training when all referenced entities exist")
        void add_success() {
            var trainee = mock(Trainee.class);
            var trainer = mock(Trainer.class);
            var type = new TrainingType("Yoga");

            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
            when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));
            when(trainingTypeRepository.findByTrainingTypeName("Yoga")).thenReturn(Optional.of(type));
            when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            service.add(validRequest());

            verify(trainingRepository).save(argThat(t ->
                    t.getTrainee() == trainee &&
                            t.getTrainer() == trainer &&
                            t.getTrainingType() == type &&
                            "Morning Yoga".equals(t.getTrainingName()) &&
                            t.getTrainingDuration() == 60
            ));
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainee not found")
        void add_traineeNotFound() {
            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.add(validRequest()))
                    .isInstanceOf(UserDoesNotExistException.class);

            verifyNoInteractions(trainerRepository, trainingTypeRepository, trainingRepository);
        }

        @Test
        @DisplayName("throws UserDoesNotExistException when trainer not found")
        void add_trainerNotFound() {
            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(mock(Trainee.class)));
            when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.add(validRequest()))
                    .isInstanceOf(UserDoesNotExistException.class);

            verifyNoInteractions(trainingTypeRepository, trainingRepository);
        }

        @Test
        @DisplayName("throws TrainingTypeNotFoundException when training type not found")
        void add_typeNotFound() {
            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(mock(Trainee.class)));
            when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(mock(Trainer.class)));
            when(trainingTypeRepository.findByTrainingTypeName("Yoga")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.add(validRequest()))
                    .isInstanceOf(TrainingTypeNotFoundException.class);

            verifyNoInteractions(trainingRepository);
        }

        @Test
        @DisplayName("sets correct training date and duration on saved entity")
        void add_trainingDateAndDuration() {
            var trainee = mock(Trainee.class);
            var trainer = mock(Trainer.class);
            var type = new TrainingType("Yoga");

            when(traineeRepository.findByUsername("trainee1")).thenReturn(Optional.of(trainee));
            when(trainerRepository.findByUsername("trainer1")).thenReturn(Optional.of(trainer));
            when(trainingTypeRepository.findByTrainingTypeName("Yoga")).thenReturn(Optional.of(type));
            when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

            service.add(validRequest());

            verify(trainingRepository).save(argThat(t ->
                    LocalDate.of(2024, 6, 1).equals(t.getTrainingDate()) &&
                            t.getTrainingDuration() == 60
            ));
        }
    }
}
