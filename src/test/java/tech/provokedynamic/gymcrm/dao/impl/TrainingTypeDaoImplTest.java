package tech.provokedynamic.gymcrm.dao.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import tech.provokedynamic.gymcrm.config.PersistenceConfig;
import tech.provokedynamic.gymcrm.dao.BaseDaoTest;
import tech.provokedynamic.gymcrm.dao.TrainingTypeDao;
import tech.provokedynamic.gymcrm.entity.TrainingType;
import tech.provokedynamic.gymcrm.exception.TrainingTypeNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ContextConfiguration(classes = {
        PersistenceConfig.class,
        TrainingTypeDaoImpl.class,
        BaseDaoTest.BaseConfig.class
})
class TrainingTypeDaoImplTest extends BaseDaoTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Test
    void findByName_returnsTrainingType_whenExists() {
        var trainingType = new TrainingType("YOGA");
        em.persist(trainingType);

        var result = trainingTypeDao.findByName("YOGA");

        assertThat(result.getTrainingTypeName()).isEqualTo("YOGA");
    }

    @Test
    void findByName_throwsTrainingTypeNotFoundException_whenNotExists() {
        assertThatThrownBy(() -> trainingTypeDao.findByName("NONEXISTENT"))
                .isInstanceOf(TrainingTypeNotFoundException.class);
    }
}
