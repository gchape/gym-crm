package tech.provokedynamic.gymcrm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.TrainingType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TraineeRepository traineeRepository;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType yoga;

    @BeforeEach
    void setUp() {
        yoga = new TrainingType("YOGA");
        em.persist(yoga);

        trainee = Trainee.builder()
                .firstName("Alice").lastName("Smith")
                .username("alice.smith").password("pass")
                .build();
        em.persist(trainee);

        trainer = Trainer.builder()
                .firstName("Bob").lastName("Jones")
                .username("bob.jones").password("pass")
                .specialization(yoga)
                .build();
        em.persist(trainer);
    }

    private Training buildTraining(String name, LocalDate date) {
        return Training.builder()
                .trainee(trainee).trainer(trainer).trainingType(yoga)
                .trainingName(name).trainingDate(date).trainingDuration(60)
                .build();
    }

    @Test
    void findByUsername_returnsTrainee_whenExists() {
        var result = traineeRepository.findByUsername("alice.smith");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice.smith");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        assertThat(traineeRepository.findByUsername("nobody")).isEmpty();
    }

    @Test
    void existsByUsername_returnsTrue_whenExists() {
        assertThat(traineeRepository.existsByUsername("alice.smith")).isTrue();
    }

    @Test
    void existsByUsername_returnsFalse_whenNotExists() {
        assertThat(traineeRepository.existsByUsername("ghost")).isFalse();
    }

    @Test
    void save_persistsChanges() {
        Trainee updated = trainee.toBuilder().firstName("Alicia").build();
        traineeRepository.save(updated);
        em.flush();
        em.clear();

        assertThat(traineeRepository.findByUsername("alice.smith")
                .orElseThrow().getFirstName()).isEqualTo("Alicia");
    }

    @Test
    void deleteByUsername_removesTrainee() {
        traineeRepository.deleteByUsername("alice.smith");
        em.flush();
        em.clear();

        assertThat(traineeRepository.findByUsername("alice.smith")).isEmpty();
    }

    @Test
    void findTrainingsByUsername_returnsAll_whenNoFilters() {
        em.persist(buildTraining("Morning Yoga", LocalDate.of(2024, 3, 10)));
        em.persist(buildTraining("Evening Yoga", LocalDate.of(2024, 3, 11)));
        em.persist(buildTraining("Night Yoga", LocalDate.of(2024, 3, 12)));

        var results = traineeRepository.findTrainingsByUsername(
                "alice.smith", null, null, null, null);

        assertThat(results).hasSize(3);
        assertThat(results).extracting("trainingName")
                .containsExactlyInAnyOrder("Morning Yoga", "Evening Yoga", "Night Yoga");
    }

    @Test
    void findTrainingsByUsername_filtersBy_fromDate() {
        em.persist(buildTraining("Old Session", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New Session", LocalDate.of(2024, 6, 1)));

        var results = traineeRepository.findTrainingsByUsername(
                "alice.smith", LocalDate.of(2024, 3, 1), null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("New Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_toDate() {
        em.persist(buildTraining("Old Session", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New Session", LocalDate.of(2024, 6, 1)));

        var results = traineeRepository.findTrainingsByUsername(
                "alice.smith", null, LocalDate.of(2024, 3, 1), null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Old Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_trainer() {
        TrainingType pilates = new TrainingType("PILATES");
        em.persist(pilates);
        Trainer other = Trainer.builder()
                .firstName("Dan").lastName("Brown")
                .username("dan.brown").password("pass")
                .specialization(pilates).build();
        em.persist(other);

        em.persist(buildTraining("Bob's Session", LocalDate.of(2024, 3, 1)));
        em.persist(Training.builder()
                .trainee(trainee).trainer(other).trainingType(pilates)
                .trainingName("Dan's Session").trainingDate(LocalDate.of(2024, 3, 2))
                .trainingDuration(30).build());

        var results = traineeRepository.findTrainingsByUsername(
                "alice.smith", null, null, "bob.jones", null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Bob's Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_type() {
        TrainingType pilates = new TrainingType("PILATES");
        em.persist(pilates);
        Trainer pilatesTrainer = Trainer.builder()
                .firstName("Eva").lastName("Green")
                .username("eva.green").password("pass")
                .specialization(pilates).build();
        em.persist(pilatesTrainer);

        em.persist(buildTraining("Yoga Class", LocalDate.of(2024, 3, 1)));
        em.persist(Training.builder()
                .trainee(trainee).trainer(pilatesTrainer).trainingType(pilates)
                .trainingName("Pilates Class").trainingDate(LocalDate.of(2024, 3, 2))
                .trainingDuration(30).build());

        var results = traineeRepository.findTrainingsByUsername(
                "alice.smith", null, null, null, "YOGA");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Yoga Class");
    }

    @Test
    void findTrainingsByUsername_returnsEmpty_whenNoTrainings() {
        assertThat(traineeRepository.findTrainingsByUsername(
                "alice.smith", null, null, null, null)).isEmpty();
    }

    @Test
    void findTrainingsByUsername_orderedByDateDesc() {
        em.persist(buildTraining("First", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("Second", LocalDate.of(2024, 6, 1)));
        em.persist(buildTraining("Third", LocalDate.of(2024, 9, 1)));

        var results = traineeRepository.findTrainingsByUsername(
                "alice.smith", null, null, null, null);

        assertThat(results).extracting("trainingName")
                .containsExactly("Third", "Second", "First");
    }
}
