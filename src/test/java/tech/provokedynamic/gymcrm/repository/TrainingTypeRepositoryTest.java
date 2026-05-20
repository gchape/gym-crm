package tech.provokedynamic.gymcrm.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import tech.provokedynamic.gymcrm.config.PersistenceConfig;
import tech.provokedynamic.gymcrm.entity.TrainingType;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
        PersistenceConfig.class,
        TrainingTypeRepository.class,
        BaseRepositoryTest.BaseConfig.class
})
class TrainingTypeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    void findByTrainingTypeName_returnsType_whenExists() {
        em.persist(new TrainingType("YOGA"));

        var result = trainingTypeRepository.findByTrainingTypeName("YOGA");

        assertThat(result).isPresent();
        assertThat(result.get().getTrainingTypeName()).isEqualTo("YOGA");
    }

    @Test
    void findByTrainingTypeName_returnsEmpty_whenNotExists() {
        assertThat(trainingTypeRepository.findByTrainingTypeName("NONEXISTENT"))
                .isEmpty();
    }
}
