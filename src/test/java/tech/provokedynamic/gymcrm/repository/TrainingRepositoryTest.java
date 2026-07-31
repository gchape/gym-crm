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

class TrainingRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

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
        var training = Training.builder()
                .trainee(trainee).trainer(trainer).trainingType(yoga)
                .trainingName("Evening Yoga")
                .trainingDate(LocalDate.of(2024, 7, 15))
                .trainingDuration(45)
                .build();

        trainingRepository.save(training);
        em.flush();
        em.clear();

        assertThat(trainingRepository.findAll()).hasSize(1);
        assertThat(trainingRepository.findAll().getFirst().getTrainingName())
                .isEqualTo("Evening Yoga");
    }

    @Test
    void save_multipleTrainings_allPersisted() {
        for (int i = 1; i <= 3; i++) {
            trainingRepository.save(Training.builder()
                    .trainee(trainee).trainer(trainer).trainingType(yoga)
                    .trainingName("Session " + i)
                    .trainingDate(LocalDate.of(2024, i, 1))
                    .trainingDuration(30 + i)
                    .build());
        }
        em.flush();
        em.clear();

        assertThat(trainingRepository.findAll()).hasSize(3);
    }
}
