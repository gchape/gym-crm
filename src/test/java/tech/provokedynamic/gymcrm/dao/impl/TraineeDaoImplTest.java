package tech.provokedynamic.gymcrm.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import tech.provokedynamic.gymcrm.config.PersistenceConfig;
import tech.provokedynamic.gymcrm.dao.impl.TraineeDaoImpl;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.TrainingType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
        PersistenceConfig.class,
        TraineeDaoImpl.class,
        BaseDaoTest.BaseConfig.class
})
class TraineeDaoImplTest extends BaseDaoTest {

    @Autowired
    private TraineeDao traineeDao;

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
    void findByUsername_returnsTrainee_whenExists() {
        var result = traineeDao.findByUsername("alice.smith");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice.smith");
    }

    @Test
    void findByUsername_returnsEmpty_whenNotExists() {
        assertThat(traineeDao.findByUsername("nobody")).isEmpty();
    }

    @Test
    void existsByUsername_returnsTrue_whenExists() {
        assertThat(traineeDao.existsByUsername("alice.smith")).isTrue();
    }

    @Test
    void existsByUsername_returnsFalse_whenNotExists() {
        assertThat(traineeDao.existsByUsername("ghost")).isFalse();
    }

    @Test
    void update_persistsChanges() {
        Trainee updated = trainee.toBuilder().firstName("Alicia").build();
        traineeDao.update(updated);
        em.flush();
        em.clear();

        assertThat(traineeDao.findByUsername("alice.smith").orElseThrow().getFirstName())
                .isEqualTo("Alicia");
    }

    @Test
    void delete_removesTrainee() {
        traineeDao.delete(trainee);
        em.flush();
        em.clear();

        assertThat(traineeDao.findByUsername("alice.smith")).isEmpty();
    }

    private Training buildTraining(String name, LocalDate date) {
        return Training.builder()
                .trainee(trainee).trainer(trainer).trainingType(yoga)
                .trainingName(name).trainingDate(date).trainingDuration(60)
                .build();
    }

    @Test
    void findTrainingsByUsername_returnsAll_whenNoFilters() {
        em.persist(buildTraining("Morning Yoga", LocalDate.of(2024, 3, 10)));
        em.persist(buildTraining("Evening Yoga", LocalDate.of(2024, 3, 11)));
        em.persist(buildTraining("Night Yoga", LocalDate.of(2024, 3, 12)));

        var results = traineeDao.findTrainingsByUsername("alice.smith", null, null, null, null);

        assertThat(results).hasSize(3);
        assertThat(results).extracting("trainingName")
                .containsExactlyInAnyOrder("Morning Yoga", "Evening Yoga", "Night Yoga");
    }

    @Test
    void findTrainingsByUsername_filtersBy_fromDate() {
        em.persist(buildTraining("Old Session", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New Session", LocalDate.of(2024, 6, 1)));

        var results = traineeDao.findTrainingsByUsername(
                "alice.smith", LocalDate.of(2024, 3, 1), null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("New Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_toDate() {
        em.persist(buildTraining("Old Session", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New Session", LocalDate.of(2024, 6, 1)));

        var results = traineeDao.findTrainingsByUsername(
                "alice.smith", null, LocalDate.of(2024, 3, 1), null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Old Session");
    }

    @Test
    void findTrainingsByUsername_filtersBy_trainer() {
        TrainingType pilates = new TrainingType("PILATES");
        em.persist(pilates);

        Trainer otherTrainer = Trainer.builder()
                .firstName("Dan").lastName("Brown")
                .username("dan.brown").password("pass")
                .specialization(pilates)
                .build();
        em.persist(otherTrainer);

        em.persist(buildTraining("Bob's Session", LocalDate.of(2024, 3, 1)));
        em.persist(Training.builder()
                .trainee(trainee).trainer(otherTrainer).trainingType(pilates)
                .trainingName("Dan's Session").trainingDate(LocalDate.of(2024, 3, 2))
                .trainingDuration(30).build());

        var results = traineeDao.findTrainingsByUsername(
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
                .specialization(pilates)
                .build();
        em.persist(pilatesTrainer);

        em.persist(buildTraining("Yoga Class", LocalDate.of(2024, 3, 1)));
        em.persist(Training.builder()
                .trainee(trainee).trainer(pilatesTrainer).trainingType(pilates)
                .trainingName("Pilates Class").trainingDate(LocalDate.of(2024, 3, 2))
                .trainingDuration(30).build());

        var results = traineeDao.findTrainingsByUsername(
                "alice.smith", null, null, null, "YOGA");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Yoga Class");
    }

    @Test
    void findTrainingsByUsername_returnsEmpty_whenNoTrainings() {
        var results = traineeDao.findTrainingsByUsername("alice.smith", null, null, null, null);

        assertThat(results).isEmpty();
    }

    @Test
    void findTrainingsByUsername_orderedByDateDesc() {
        em.persist(buildTraining("First", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("Second", LocalDate.of(2024, 6, 1)));
        em.persist(buildTraining("Third", LocalDate.of(2024, 9, 1)));

        var results = traineeDao.findTrainingsByUsername("alice.smith", null, null, null, null);

        assertThat(results).extracting("trainingName")
                .containsExactly("Third", "Second", "First");
    }

    @Test
    void findUnassignedTrainers_includesTrainer_whenNotAssigned() {
        var result = traineeDao.findUnassignedTrainers("alice.smith");

        assertThat(result).extracting("username").contains("bob.jones");
    }

    @Test
    void findUnassignedTrainers_excludesTrainer_whenAlreadyAssigned() {
        trainee.getTrainers().add(trainer);
        em.merge(trainee);
        em.flush();
        em.clear();

        var result = traineeDao.findUnassignedTrainers("alice.smith");

        assertThat(result).extracting("username").doesNotContain("bob.jones");
    }

    @Test
    void findAssignedTrainers_returnsTrainer_whenAssigned() {
        trainee.getTrainers().add(trainer);
        em.merge(trainee);
        em.flush();
        em.clear();

        var result = traineeDao.findAssignedTrainers("alice.smith");

        assertThat(result).extracting("username").containsExactly("bob.jones");
    }

    @Test
    void findAssignedTrainers_returnsEmpty_whenNoneAssigned() {
        var result = traineeDao.findAssignedTrainers("alice.smith");

        assertThat(result).isEmpty();
    }
}
