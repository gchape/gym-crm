package tech.provokedynamic.gymcrm.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrm.component.InMemoryStorage;
import tech.provokedynamic.gymcrm.entity.Trainee;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TraineeDaoTest {

    private TraineeDao traineeDao;

    @BeforeEach
    void setUp() {
        var storage = new InMemoryStorage();
        traineeDao = new TraineeDao(storage);
    }

    @Test
    void shouldReturnEmptyWhenEntityDoesNotExist() {
        Optional<Trainee> result = traineeDao.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSaveAndFindById() {
        Trainee trainee = Trainee.builder().build();

        traineeDao.save(1L, trainee);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertThat(result)
                .isPresent()
                .containsSame(trainee);
    }

    @Test
    void shouldReturnAllSavedEntities() {
        Trainee t1 = Trainee.builder().build();
        Trainee t2 = Trainee.builder().build();

        traineeDao.save(1L, t1);
        traineeDao.save(2L, t2);

        List<Trainee> result = traineeDao.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(t1, t2);
    }

    @Test
    void shouldOverwriteEntityOnUpdate() {
        Trainee original = Trainee.builder().build();
        Trainee updated = Trainee.builder().build();

        traineeDao.save(1L, original);
        traineeDao.update(1L, updated);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertThat(result)
                .isPresent()
                .containsSame(updated);
    }

    @Test
    void shouldDeleteEntity() {
        Trainee trainee = Trainee.builder().build();

        traineeDao.save(1L, trainee);
        traineeDao.delete(1L);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertThat(result).isEmpty();
    }
}
