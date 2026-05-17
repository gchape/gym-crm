package tech.provokedynamic.gymcrm.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import tech.provokedynamic.gymcrm.config.PersistenceConfig;
import tech.provokedynamic.gymcrm.dao.impl.TrainingDaoImpl;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.entity.TrainingType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
        PersistenceConfig.class,
        TrainingDaoImpl.class,
        BaseDaoTest.BaseConfig.class
})
class TrainingDaoImplTest extends BaseDaoTest {

    @Autowired
    private TrainingDao trainingDao;

    private TrainingType yoga;
    private Trainee trainee;
    private Trainer trainer;

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
    void save_persistsTraining() {
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(yoga)
                .trainingName("Evening Yoga")
                .trainingDate(LocalDate.of(2024, 7, 15))
                .trainingDuration(45)
                .build();

        trainingDao.save(training);
        em.flush();
        em.clear();

        long count = em.createQuery("SELECT count(t) FROM Training t", Long.class)
                .getSingleResult();
        assertThat(count).isEqualTo(1);

        Training saved = em.createQuery("SELECT t FROM Training t", Training.class)
                .getSingleResult();
        assertThat(saved.getTrainingName()).isEqualTo("Evening Yoga");
        assertThat(saved.getTrainingDuration()).isEqualTo(45);
    }

    @Test
    void save_multipleTrainings_allPersisted() {
        for (int i = 1; i <= 3; i++) {
            trainingDao.save(Training.builder()
                    .trainee(trainee)
                    .trainer(trainer)
                    .trainingType(yoga)
                    .trainingName("Session " + i)
                    .trainingDate(LocalDate.of(2024, i, 1))
                    .trainingDuration(30 + i)
                    .build());
        }

        em.flush();
        em.clear();

        long count = em.createQuery("SELECT count(t) FROM Training t", Long.class)
                .getSingleResult();
        assertThat(count).isEqualTo(3);
    }
}
