package tech.provokedynamic.gymcrm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import tech.provokedynamic.gymcrm.config.PersistenceConfig;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.TrainingType;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
        PersistenceConfig.class,
        TrainerRepository.class,
        BaseRepositoryTest.BaseConfig.class
})
class TrainerRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TrainerRepository trainerRepository;

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
    void findByUsername_returnsTrainer_whenExists() {
        assertThat(trainerRepository.findByUsername("bob.jones")).isPresent();
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        assertThat(trainerRepository.findByUsername("ghost")).isEmpty();
    }

    @Test
    void existsByUsername_returnsTrue_whenExists() {
        assertThat(trainerRepository.existsByUsername("bob.jones")).isTrue();
    }

    @Test
    void existsByUsername_returnsFalse_whenNotExists() {
        assertThat(trainerRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void save_persistsChanges() {
        Trainer updated = trainer.toBuilder().firstName("Robert").build();
        trainerRepository.save(updated);
        em.flush();
        em.clear();

        assertThat(trainerRepository.findByUsername("bob.jones")
                .orElseThrow().getFirstName()).isEqualTo("Robert");
    }

    @Test
    void findAllByUsernameIn_returnsMatchingTrainers() {
        Trainer other = Trainer.builder()
                .firstName("Carol").lastName("White")
                .username("carol.white").password("pass")
                .specialization(yoga).build();
        em.persist(other);
        em.flush();

        var results = trainerRepository.findAllByUsernameIn(
                Set.of("bob.jones", "carol.white"));

        assertThat(results).extracting("username")
                .containsExactlyInAnyOrder("bob.jones", "carol.white");
    }

    @Test
    void findAllByUsernameIn_returnsEmpty_whenNoneMatch() {
        assertThat(trainerRepository.findAllByUsernameIn(
                Set.of("ghost1", "ghost2"))).isEmpty();
    }

    @Test
    void findTrainingsByUsername_returnsAll_whenNoFilters() {
        em.persist(buildTraining("Morning Session", LocalDate.of(2024, 5, 1)));
        em.persist(buildTraining("Afternoon Session", LocalDate.of(2024, 5, 2)));
        em.flush();

        var results = trainerRepository.findTrainingsByUsername(
                "bob.jones", null, null, null);

        assertThat(results).hasSize(2);
    }

    @Test
    void findTrainingsByUsername_filtersBy_fromDate() {
        em.persist(buildTraining("Old Session", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New Session", LocalDate.of(2024, 8, 1)));

        var results = trainerRepository.findTrainingsByUsername(
                "bob.jones", LocalDate.of(2024, 4, 1), null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("New Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_toDate() {
        em.persist(buildTraining("Old Session", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New Session", LocalDate.of(2024, 8, 1)));

        var results = trainerRepository.findTrainingsByUsername(
                "bob.jones", null, LocalDate.of(2024, 4, 1), null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Old Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_trainee() {
        Trainee other = Trainee.builder()
                .firstName("Dan").lastName("Brown")
                .username("dan.brown").password("pass").build();
        em.persist(other);

        em.persist(buildTraining("Alice's Session", LocalDate.of(2024, 5, 1)));
        em.persist(Training.builder()
                .trainee(other).trainer(trainer).trainingType(yoga)
                .trainingName("Dan's Session").trainingDate(LocalDate.of(2024, 5, 2))
                .trainingDuration(30).build());

        var results = trainerRepository.findTrainingsByUsername(
                "bob.jones", null, null, "alice.smith");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Alice's Session");
    }

    @Test
    void findTrainingsByUsername_returnsEmpty_whenNoTrainings() {
        assertThat(trainerRepository.findTrainingsByUsername(
                "bob.jones", null, null, null)).isEmpty();
    }

    @Test
    void findTrainingsByUsername_orderedByDateDesc() {
        em.persist(buildTraining("First", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("Second", LocalDate.of(2024, 6, 1)));
        em.persist(buildTraining("Third", LocalDate.of(2024, 9, 1)));

        var results = trainerRepository.findTrainingsByUsername(
                "bob.jones", null, null, null);

        assertThat(results).extracting("trainingName")
                .containsExactly("Third", "Second", "First");
    }
}
