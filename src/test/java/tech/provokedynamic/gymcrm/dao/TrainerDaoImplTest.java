package tech.provokedynamic.gymcrm.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import tech.provokedynamic.gymcrm.config.PersistenceConfig;
import tech.provokedynamic.gymcrm.dao.impl.TrainerDaoImpl;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.TrainingType;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
        PersistenceConfig.class,
        TrainerDaoImpl.class,
        BaseDaoTest.BaseConfig.class
})
class TrainerDaoImplTest extends BaseDaoTest {

    @Autowired
    private TrainerDao trainerDao;

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

    @Test
    void findByUsername_returnsTrainer_whenExists() {
        var result = trainerDao.findByUsername("bob.jones");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("bob.jones");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        assertThat(trainerDao.findByUsername("ghost")).isEmpty();
    }

    @Test
    void existsByUsername_returnsTrue_whenExists() {
        assertThat(trainerDao.existsByUsername("bob.jones")).isTrue();
    }

    @Test
    void existsByUsername_returnsFalse_whenNotExists() {
        assertThat(trainerDao.existsByUsername("nobody")).isFalse();
    }

    @Test
    void update_persistsChanges() {
        Trainer updated = trainer.toBuilder().firstName("Robert").build();
        trainerDao.update(updated);
        em.flush();
        em.clear();

        assertThat(trainerDao.findByUsername("bob.jones").orElseThrow().getFirstName())
                .isEqualTo("Robert");
    }

    @Test
    void findByUsernames_returnsMatchingTrainers() {
        Trainer other = Trainer.builder()
                .firstName("Carol").lastName("White")
                .username("carol.white").password("pass")
                .specialization(yoga)
                .build();
        em.persist(other);
        em.flush();

        var results = trainerDao.findByUsernames(List.of("bob.jones", "carol.white"));

        assertThat(results).extracting("username")
                .containsExactlyInAnyOrder("bob.jones", "carol.white");
    }

    @Test
    void findByUsernames_returnsEmpty_whenNoneMatch() {
        var results = trainerDao.findByUsernames(List.of("ghost1", "ghost2"));

        assertThat(results).isEmpty();
    }

    @Test
    void findByUsernames_returnsOnlyExisting_whenSomeMissing() {
        var results = trainerDao.findByUsernames(List.of("bob.jones", "ghost"));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getUsername()).isEqualTo("bob.jones");
    }

    private Training buildTraining(String name, LocalDate date) {
        return Training.builder()
                .trainee(trainee).trainer(trainer).trainingType(yoga)
                .trainingName(name).trainingDate(date).trainingDuration(60)
                .build();
    }

    @Test
    void findTrainingsByUsername_returnsAll_whenNoFilters() {
        em.persist(buildTraining("Morning Session", LocalDate.of(2024, 5, 1)));
        em.flush();

        var results = trainerDao.findTrainingsByUsername("bob.jones", null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Morning Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_fromDate() {
        em.persist(buildTraining("Old Session", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New Session", LocalDate.of(2024, 8, 1)));
        em.flush();

        var results = trainerDao.findTrainingsByUsername(
                "bob.jones", LocalDate.of(2024, 4, 1), null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("New Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_toDate() {
        em.persist(buildTraining("Old Session", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New Session", LocalDate.of(2024, 8, 1)));
        em.flush();

        var results = trainerDao.findTrainingsByUsername(
                "bob.jones", null, LocalDate.of(2024, 4, 1), null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Old Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_trainee() {
        Trainee otherTrainee = Trainee.builder()
                .firstName("Dan").lastName("Brown")
                .username("dan.brown").password("pass")
                .build();
        em.persist(otherTrainee);

        em.persist(buildTraining("Alice's Session", LocalDate.of(2024, 5, 1)));
        em.persist(Training.builder()
                .trainee(otherTrainee).trainer(trainer).trainingType(yoga)
                .trainingName("Dan's Session").trainingDate(LocalDate.of(2024, 5, 2))
                .trainingDuration(30).build());
        em.flush();

        var results = trainerDao.findTrainingsByUsername(
                "bob.jones", null, null, "alice.smith");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Alice's Session");
    }

    @Test
    void findTrainingsByUsername_returnsEmpty_whenNoTrainings() {
        var results = trainerDao.findTrainingsByUsername("bob.jones", null, null, null);

        assertThat(results).isEmpty();
    }

    @Test
    void findTrainingsByUsername_orderedByDateDesc() {
        em.persist(buildTraining("First", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("Second", LocalDate.of(2024, 6, 1)));
        em.flush();

        var results = trainerDao.findTrainingsByUsername("bob.jones", null, null, null);

        assertThat(results).extracting("trainingName")
                .containsExactly("Second", "First");
    }
}
