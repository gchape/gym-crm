package tech.provokedynamic.gymcrm.dao.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import tech.provokedynamic.gymcrm.config.PersistenceConfig;
import tech.provokedynamic.gymcrm.dao.BaseDaoTest;
import tech.provokedynamic.gymcrm.dto.Summary;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.model.UserType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
        PersistenceConfig.class,
        BaseDaoTest.BaseConfig.class
})
class AbstractUserDaoImplTest extends BaseDaoTest {

    private TestDao dao;

    private Trainee trainee;

    private Trainer trainer;

    private TrainingType yoga;

    @BeforeEach
    void setUp() {
        dao = new TestDao(em);

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
    void trainee_returnsAllTrainings_whenNoFilters() {
        em.persist(buildTraining("Session A", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("Session B", LocalDate.of(2024, 6, 1)));
        em.persist(buildTraining("Session C", LocalDate.of(2024, 9, 1)));

        var results = dao.findTrainings("alice.smith", UserType.TRAINEE, null, null, null, null);

        assertThat(results).hasSize(3);
        assertThat(results).extracting("trainingName")
                .containsExactlyInAnyOrder("Session A", "Session B", "Session C");
    }

    @Test
    void trainee_filtersBy_fromDate() {
        em.persist(buildTraining("Old", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New", LocalDate.of(2024, 8, 1)));

        var results = dao.findTrainings(
                "alice.smith", UserType.TRAINEE, LocalDate.of(2024, 4, 1), null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("New");
    }

    @Test
    void trainee_filtersBy_toDate() {
        em.persist(buildTraining("Old", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New", LocalDate.of(2024, 8, 1)));

        var results = dao.findTrainings(
                "alice.smith", UserType.TRAINEE, null, LocalDate.of(2024, 4, 1), null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Old");
    }

    @Test
    void trainee_filtersBy_trainerUsername() {
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

        var results = dao.findTrainings(
                "alice.smith", UserType.TRAINEE, null, null, "bob.jones", null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Bob's Session");
    }

    @Test
    void save_persistsUser() {
        Trainee newTrainee = Trainee.builder()
                .firstName("Eve").lastName("Black")
                .username("eve.black").password("secret")
                .build();

        dao.save(newTrainee);
        em.flush();
        em.clear();

        assertThat(em.find(Trainee.class, newTrainee.getId())).isNotNull();
    }

    @Test
    void existsByUsernameIncludingDeleted_returnsTrue_whenExists() {
        assertThat(dao.existsByUsernameIncludingDeleted("alice.smith")).isTrue();
    }

    @Test
    void existsByUsernameIncludingDeleted_returnsFalse_whenAbsent() {
        assertThat(dao.existsByUsernameIncludingDeleted("nobody.here")).isFalse();
    }

    @Test
    void existsByUsernameAndPassword_returnsTrue_whenCredentialsMatch() {
        assertThat(dao.existsByUsernameAndPassword("alice.smith", "pass")).isTrue();
    }

    @Test
    void existsByUsernameAndPassword_returnsFalse_whenPasswordWrong() {
        assertThat(dao.existsByUsernameAndPassword("alice.smith", "wrong")).isFalse();
    }

    @Test
    void existsByUsernameAndPassword_returnsFalse_whenUsernameUnknown() {
        assertThat(dao.existsByUsernameAndPassword("nobody.here", "pass")).isFalse();
    }


    @Test
    void updatePassword_changesPassword() {
        dao.updatePassword("alice.smith", "newSecret");
        em.flush();

        assertThat(dao.existsByUsernameAndPassword("alice.smith", "newSecret")).isTrue();
        assertThat(dao.existsByUsernameAndPassword("alice.smith", "pass")).isFalse();
    }

    @Test
    void deactivateByUsername_setsActiveToFalse() {
        int affected = dao.deactivateByUsername("alice.smith");
        em.flush();
        em.clear();

        assertThat(affected).isEqualTo(1);
        assertThat(em.find(Trainee.class, trainee.getId())).isNull();
    }

    @Test
    void deactivateByUsername_isNoOp_whenAlreadyInactive() {
        dao.deactivateByUsername("alice.smith");
        em.flush();

        assertThat(dao.deactivateByUsername("alice.smith")).isEqualTo(0);
    }

    @Test
    void activateByUsername_setsActiveToTrue() {
        dao.deactivateByUsername("alice.smith");
        em.flush();

        int affected = dao.activateByUsername("alice.smith");
        em.flush();
        em.clear();

        assertThat(affected).isEqualTo(1);
        assertThat(em.find(Trainee.class, trainee.getId())).isNotNull();
    }

    @Test
    void activateByUsername_isNoOp_whenAlreadyActive() {
        assertThat(dao.activateByUsername("alice.smith")).isEqualTo(0);
    }

    @Test
    void trainee_filtersBy_fromAndToDate_combined() {
        em.persist(buildTraining("Too early", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("In range", LocalDate.of(2024, 5, 15)));
        em.persist(buildTraining("Too late", LocalDate.of(2024, 9, 1)));

        var results = dao.findTrainings(
                "alice.smith", UserType.TRAINEE,
                LocalDate.of(2024, 4, 1), LocalDate.of(2024, 6, 1),
                null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("In range");
    }

    @Test
    void trainer_filtersBy_fromDate() {
        em.persist(buildTraining("Old", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New", LocalDate.of(2024, 8, 1)));

        var results = dao.findTrainings(
                "bob.jones", UserType.TRAINER,
                LocalDate.of(2024, 4, 1), null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("New");
    }

    @Test
    void trainer_filtersBy_toDate() {
        em.persist(buildTraining("Old", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("New", LocalDate.of(2024, 8, 1)));

        var results = dao.findTrainings(
                "bob.jones", UserType.TRAINER,
                null, LocalDate.of(2024, 4, 1), null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Old");
    }

    @Test
    void trainer_filtersBy_trainingType() {
        TrainingType pilates = new TrainingType("PILATES");
        em.persist(pilates);

        Trainer pilatesTrainer = Trainer.builder()
                .firstName("Eva").lastName("Green")
                .username("eva.green").password("pass")
                .specialization(pilates)
                .build();
        em.persist(pilatesTrainer);

        em.persist(buildTraining("Yoga with Bob", LocalDate.of(2024, 3, 1)));
        em.persist(Training.builder()
                .trainee(trainee).trainer(pilatesTrainer).trainingType(pilates)
                .trainingName("Pilates with Eva").trainingDate(LocalDate.of(2024, 3, 2))
                .trainingDuration(30).build());

        var results = dao.findTrainings(
                "bob.jones", UserType.TRAINER, null, null, null, "YOGA");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Yoga with Bob");
    }

    @Test
    void trainer_returnsEmpty_whenNoTrainings() {
        var results = dao.findTrainings("bob.jones", UserType.TRAINER, null, null, null, null);

        assertThat(results).isEmpty();
    }

    @Test
    void trainee_filtersBy_trainingType() {
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

        var results = dao.findTrainings(
                "alice.smith", UserType.TRAINEE, null, null, null, "YOGA");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Yoga Class");
    }

    @Test
    void trainee_returnsEmpty_whenNoTrainings() {
        var results = dao.findTrainings("alice.smith", UserType.TRAINEE, null, null, null, null);

        assertThat(results).isEmpty();
    }

    @Test
    void trainee_orderedByDateDesc() {
        em.persist(buildTraining("First", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("Second", LocalDate.of(2024, 6, 1)));
        em.persist(buildTraining("Third", LocalDate.of(2024, 9, 1)));

        var results = dao.findTrainings("alice.smith", UserType.TRAINEE, null, null, null, null);

        assertThat(results).extracting("trainingName")
                .containsExactly("Third", "Second", "First");
    }

    @Test
    void trainer_returnsAllTrainings_whenNoFilters() {
        em.persist(buildTraining("Session A", LocalDate.of(2024, 1, 1)));
        em.persist(buildTraining("Session B", LocalDate.of(2024, 6, 1)));

        var results = dao.findTrainings("bob.jones", UserType.TRAINER, null, null, null, null);

        assertThat(results).hasSize(2);
    }

    @Test
    void trainer_filtersBy_traineeUsername() {
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

        var results = dao.findTrainings(
                "bob.jones", UserType.TRAINER, null, null, "alice.smith", null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Alice's Session");
    }

    @Test
    void trainer_doesNotReturn_otherTrainersTrainings() {
        Trainer otherTrainer = Trainer.builder()
                .firstName("Dan").lastName("Brown")
                .username("dan.brown").password("pass")
                .specialization(yoga)
                .build();
        em.persist(otherTrainer);

        em.persist(buildTraining("Bob's Session", LocalDate.of(2024, 5, 1)));
        em.persist(Training.builder()
                .trainee(trainee).trainer(otherTrainer).trainingType(yoga)
                .trainingName("Dan's Session").trainingDate(LocalDate.of(2024, 5, 2))
                .trainingDuration(30).build());

        var results = dao.findTrainings("bob.jones", UserType.TRAINER, null, null, null, null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().trainingName()).isEqualTo("Bob's Session");
    }

    @Test
    void traineeAndTrainer_doNotReturnEachOthersData() {
        Trainee otherTrainee = Trainee.builder()
                .firstName("Dan").lastName("Brown")
                .username("dan.brown").password("pass")
                .build();
        em.persist(otherTrainee);

        Trainer otherTrainer = Trainer.builder()
                .firstName("Eva").lastName("Green")
                .username("eva.green").password("pass")
                .specialization(yoga)
                .build();
        em.persist(otherTrainer);

        em.persist(buildTraining("Alice+Bob", LocalDate.of(2024, 1, 1)));
        em.persist(Training.builder()
                .trainee(otherTrainee).trainer(otherTrainer).trainingType(yoga)
                .trainingName("Dan+Eva").trainingDate(LocalDate.of(2024, 2, 1))
                .trainingDuration(30).build());

        var aliceResults = dao.findTrainings(
                "alice.smith", UserType.TRAINEE, null, null, null, null);
        var bobResults = dao.findTrainings(
                "bob.jones", UserType.TRAINER, null, null, null, null);

        assertThat(aliceResults).extracting("trainingName").containsExactly("Alice+Bob");
        assertThat(bobResults).extracting("trainingName").containsExactly("Alice+Bob");
    }

    static class TestDao extends AbstractUserDaoImpl {
        TestDao(jakarta.persistence.EntityManager em) {
            super(em);
        }

        public java.util.List<Summary.Training> findTrainings(
                String username,
                UserType userType,
                java.time.LocalDate from,
                java.time.LocalDate to,
                String filterUsername,
                String type
        ) {
            return super.findTrainingsByUsername(username, userType, from, to, filterUsername, type);
        }
    }
}
