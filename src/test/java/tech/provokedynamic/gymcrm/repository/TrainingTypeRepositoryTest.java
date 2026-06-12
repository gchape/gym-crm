package tech.provokedynamic.gymcrm.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import tech.provokedynamic.gymcrm.entity.TrainingType;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
