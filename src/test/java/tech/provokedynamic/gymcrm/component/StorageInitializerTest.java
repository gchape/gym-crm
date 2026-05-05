package tech.provokedynamic.gymcrm.component;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.storage.Storage;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        useMainMethod = SpringBootTest.UseMainMethod.NEVER,
        properties = "storage.data.path=classpath:data/test-data.json")
class StorageInitializerTest {

    @Autowired
    private Storage<Entity> storage;

    @Test
    void shouldLoadThreeTrainees_whenDataFileIsValid() {
        assertThat(storage.getNamespace("trainee")).hasSize(3);
    }

    @Test
    void shouldLoadThreeTrainers_whenDataFileIsValid() {
        assertThat(storage.getNamespace("trainer")).hasSize(3);
    }

    @Test
    void shouldLoadThreeTrainings_whenDataFileIsValid() {
        assertThat(storage.getNamespace("training")).hasSize(3);
    }

    @Test
    void shouldLoadTraineesAsCorrectType_whenDataFileIsValid() {
        assertThat(storage.getNamespace("trainee"))
                .allSatisfy((id, entity) -> assertThat(entity).isInstanceOf(Trainee.class));
    }

    @Test
    void shouldLoadTrainersAsCorrectType_whenDataFileIsValid() {
        assertThat(storage.getNamespace("trainer"))
                .allSatisfy((id, entity) -> assertThat(entity).isInstanceOf(Trainer.class));
    }

    @Test
    void shouldLoadTrainingsAsCorrectType_whenDataFileIsValid() {
        assertThat(storage.getNamespace("training"))
                .allSatisfy((id, entity) -> assertThat(entity).isInstanceOf(Training.class));
    }

    @Test
    void shouldNotPolluteCrossNamespace_whenStorageIsLoaded() {
        assertThat(storage.getNamespace("trainee"))
                .allSatisfy((id, entity) -> assertThat(entity).isInstanceOf(Trainee.class));
        assertThat(storage.getNamespace("trainer"))
                .allSatisfy((id, entity) -> assertThat(entity).isInstanceOf(Trainer.class));
        assertThat(storage.getNamespace("training"))
                .allSatisfy((id, entity) -> assertThat(entity).isInstanceOf(Training.class));
    }

    @Test
    void shouldLoadTraineeWithCorrectFields_whenDataFileIsValid() {
        Trainee trainee = (Trainee) storage.get("trainee", 1L);

        assertThat(trainee).isNotNull();
        assertThat(trainee.getFirstName()).isEqualTo("John");
        assertThat(trainee.getLastName()).isEqualTo("Smith");
        assertThat(trainee.getUsername()).isEqualTo("John.Smith");
        assertThat(trainee.isActive()).isTrue();
    }

    @Test
    void shouldLoadTrainerWithCorrectFields_whenDataFileIsValid() {
        Trainer trainer = (Trainer) storage.get("trainer", 1L);

        assertThat(trainer).isNotNull();
        assertThat(trainer.getFirstName()).isEqualTo("Mike");
        assertThat(trainer.getLastName()).isEqualTo("Johnson");
        assertThat(trainer.getUsername()).isEqualTo("Mike.Johnson");
        assertThat(trainer.isActive()).isTrue();
    }

    @Test
    void shouldLoadTrainingWithCorrectFields_whenDataFileIsValid() {
        Training training = (Training) storage.get("training", 1L);

        assertThat(training).isNotNull();
        assertThat(training.trainingName()).isEqualTo("Morning Strength Session");
        assertThat(training.traineeId()).isEqualTo(1L);
        assertThat(training.trainerId()).isEqualTo(1L);
    }

    @Test
    void shouldLoadTraineeWithSuffixedUsername_whenDuplicateFirstAndLastName() {
        Trainee trainee = (Trainee) storage.get("trainee", 3L);

        assertThat(trainee).isNotNull();
        assertThat(trainee.getUsername()).isEqualTo("John.Smith1");
    }

    @Test
    void shouldLoadTrainerWithSuffixedUsername_whenDuplicateFirstAndLastName() {
        Trainer trainer = (Trainer) storage.get("trainer", 3L);

        assertThat(trainer).isNotNull();
        assertThat(trainer.getUsername()).isEqualTo("Mike.Johnson1");
    }
}