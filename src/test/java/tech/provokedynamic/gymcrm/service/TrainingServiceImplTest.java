package tech.provokedynamic.gymcrm.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.annotation.DirtiesContext;
import tech.provokedynamic.gymcrm.aspect.ValidationAspect;
import tech.provokedynamic.gymcrm.component.InMemoryStorage;
import tech.provokedynamic.gymcrm.dao.TrainingDao;
import tech.provokedynamic.gymcrm.dto.TrainingRequest;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.model.TrainingType;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {
        TrainingServiceImpl.class,
        TrainingDao.class,
        ValidationAspect.class,
        InMemoryStorage.class
}, webEnvironment = SpringBootTest.WebEnvironment.NONE,
        useMainMethod = SpringBootTest.UseMainMethod.NEVER)
@EnableAspectJAutoProxy
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TrainingServiceImplTest {

    @Autowired
    private TrainingService trainingService;

    private TrainingRequest.Create validCreateRequest() {
        return new TrainingRequest.Create(
                1L, 1L, "Morning Cardio",
                TrainingType.CARDIO,
                LocalDate.now(),
                Duration.ofHours(1)
        );
    }

    @Test
    void shouldCreateTraining_whenRequestIsValid() {
        Training result = trainingService.create(validCreateRequest());

        assertThat(result).isNotNull();
        assertThat(result.trainingName()).isEqualTo("Morning Cardio");
        assertThat(result.trainingType()).isEqualTo(TrainingType.CARDIO);
        assertThat(result.traineeId()).isEqualTo(1L);
        assertThat(result.trainerId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowException_whenTrainingNameIsBlank() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 1L, "", TrainingType.CARDIO, LocalDate.now(), Duration.ofHours(1)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenTrainingTypeIsNull() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 1L, "Morning Cardio", null, LocalDate.now(), Duration.ofHours(1)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenTrainingDateIsNull() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 1L, "Morning Cardio", TrainingType.CARDIO, null, Duration.ofHours(1)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenTrainingDateIsInThePast() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 1L, "Morning Cardio",
                TrainingType.CARDIO,
                LocalDate.now().minusDays(1),
                Duration.ofHours(1)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldCreateTraining_whenTrainingDateIsToday() {
        Training result = trainingService.create(validCreateRequest());

        assertThat(result.trainingDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldCreateTraining_whenTrainingDateIsInTheFuture() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 1L, "Morning Cardio",
                TrainingType.CARDIO,
                LocalDate.now().plusDays(7),
                Duration.ofHours(1)
        );

        Training result = trainingService.create(request);

        assertThat(result.trainingDate()).isEqualTo(LocalDate.now().plusDays(7));
    }

    @Test
    void shouldThrowException_whenDurationIsNull() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 1L, "Morning Cardio", TrainingType.CARDIO, LocalDate.now(), null
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenDurationIsBelowMinimum() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 1L, "Morning Cardio",
                TrainingType.CARDIO,
                LocalDate.now(),
                Duration.ofMinutes(29)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldCreateTraining_whenDurationIsAtMinimum() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 1L, "Morning Cardio",
                TrainingType.CARDIO,
                LocalDate.now(),
                Duration.ofMinutes(30)
        );

        Training result = trainingService.create(request);

        assertThat(result.trainingDuration()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void shouldThrowException_whenTraineeIdIsZero() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                0L, 1L, "Morning Cardio", TrainingType.CARDIO, LocalDate.now(), Duration.ofHours(1)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenTrainerIdIsZero() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, 0L, "Morning Cardio", TrainingType.CARDIO, LocalDate.now(), Duration.ofHours(1)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenTraineeIdIsNegative() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                -1L, 1L, "Morning Cardio", TrainingType.CARDIO, LocalDate.now(), Duration.ofHours(1)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldThrowException_whenTrainerIdIsNegative() {
        TrainingRequest.Create request = new TrainingRequest.Create(
                1L, -1L, "Morning Cardio", TrainingType.CARDIO, LocalDate.now(), Duration.ofHours(1)
        );

        assertThatThrownBy(() -> trainingService.create(request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void shouldFindTrainingById_whenTrainingExists() {
        Training created = trainingService.create(validCreateRequest());

        Training found = trainingService.findById(1L);

        assertThat(found).isEqualTo(created);
    }

    @Test
    void shouldThrowException_whenFindByIdAndTrainingDoesNotExist() {
        assertThatThrownBy(() -> trainingService.findById(999999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnAllTrainings_whenFindAllCalled() {
        trainingService.create(validCreateRequest());
        trainingService.create(new TrainingRequest.Create(
                2L, 2L, "Evening Yoga",
                TrainingType.YOGA,
                LocalDate.now().plusDays(1),
                Duration.ofHours(1)
        ));

        List<Training> result = trainingService.findAll();

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }
}