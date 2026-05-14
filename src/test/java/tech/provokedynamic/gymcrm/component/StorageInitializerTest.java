package tech.provokedynamic.gymcrm.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.provokedynamic.gymcrm.config.JacksonConfig;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.storage.Storage;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        StorageInitializer.class,
        InMemoryStorage.class,
        JacksonConfig.class,
        StorageKeyFormatter.class
})
@TestPropertySource(locations = "classpath:application.properties")
class StorageInitializerTest {

    @Autowired
    private Storage<Entity> storage;

    @Test
    void shouldLoadThreeTrainees_whenDataFileIsValid() {
        assertThat(storage.getNamespace(Storage.Namespace.TRAINEE)).hasSize(3);
    }

    @Test
    void shouldLoadThreeTrainers_whenDataFileIsValid() {
        assertThat(storage.getNamespace(Storage.Namespace.TRAINER)).hasSize(3);
    }

    @Test
    void shouldLoadThreeTrainings_whenDataFileIsValid() {
        assertThat(storage.getNamespace(Storage.Namespace.TRAINING)).hasSize(3);
    }

    @Test
    void shouldLoadTraineesAsCorrectType_whenDataFileIsValid() {
        assertThat(storage.getNamespace(Storage.Namespace.TRAINEE))
                .allSatisfy((_, entity) -> assertThat(entity).isInstanceOf(Trainee.class));
    }

    @Test
    void shouldLoadTrainersAsCorrectType_whenDataFileIsValid() {
        assertThat(storage.getNamespace(Storage.Namespace.TRAINER))
                .allSatisfy((_, entity) -> assertThat(entity).isInstanceOf(Trainer.class));
    }

    @Test
    void shouldLoadTrainingsAsCorrectType_whenDataFileIsValid() {
        assertThat(storage.getNamespace(Storage.Namespace.TRAINING))
                .allSatisfy((_, entity) -> assertThat(entity).isInstanceOf(Training.class));
    }

    @Test
    void shouldNotPolluteCrossNamespace_whenStorageIsLoaded() {
        assertThat(storage.getNamespace(Storage.Namespace.TRAINEE))
                .allSatisfy((_, entity) -> assertThat(entity).isInstanceOf(Trainee.class));
        assertThat(storage.getNamespace(Storage.Namespace.TRAINER))
                .allSatisfy((_, entity) -> assertThat(entity).isInstanceOf(Trainer.class));
        assertThat(storage.getNamespace(Storage.Namespace.TRAINING))
                .allSatisfy((_, entity) -> assertThat(entity).isInstanceOf(Training.class));
    }

    @Test
    void shouldLoadTraineeWithCorrectFields_whenDataFileIsValid() {
        Trainee trainee = (Trainee) storage.get(Storage.Namespace.TRAINEE, 1L);

        assertThat(trainee).isNotNull();
        assertThat(trainee.firstName()).isEqualTo("John");
        assertThat(trainee.lastName()).isEqualTo("Smith");
        assertThat(trainee.username()).isEqualTo("John.Smith");
        assertThat(trainee.isActive()).isTrue();
    }

    @Test
    void shouldLoadTrainerWithCorrectFields_whenDataFileIsValid() {
        Trainer trainer = (Trainer) storage.get(Storage.Namespace.TRAINER, 1L);

        assertThat(trainer).isNotNull();
        assertThat(trainer.firstName()).isEqualTo("Mike");
        assertThat(trainer.lastName()).isEqualTo("Johnson");
        assertThat(trainer.username()).isEqualTo("Mike.Johnson");
        assertThat(trainer.isActive()).isTrue();
    }

    @Test
    void shouldLoadTrainingWithCorrectFields_whenDataFileIsValid() {
        Training training = (Training) storage.get(Storage.Namespace.TRAINING, 1L);

        assertThat(training).isNotNull();
        assertThat(training.trainingName()).isEqualTo("Morning Strength Session");
        assertThat(training.traineeId()).isEqualTo(1L);
        assertThat(training.trainerId()).isEqualTo(1L);
    }

    @Test
    void shouldLoadTraineeWithSuffixedUsername_whenDuplicateFirstAndLastName() {
        Trainee trainee = (Trainee) storage.get(Storage.Namespace.TRAINEE, 3L);

        assertThat(trainee).isNotNull();
        assertThat(trainee.username()).isEqualTo("John.Smith1");
    }

    @Test
    void shouldLoadTrainerWithSuffixedUsername_whenDuplicateFirstAndLastName() {
        Trainer trainer = (Trainer) storage.get(Storage.Namespace.TRAINER, 3L);

        assertThat(trainer).isNotNull();
        assertThat(trainer.username()).isEqualTo("Mike.Johnson1");
    }
}
