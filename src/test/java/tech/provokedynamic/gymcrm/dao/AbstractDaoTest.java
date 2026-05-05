package tech.provokedynamic.gymcrm.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrm.component.InMemoryStorage;
import tech.provokedynamic.gymcrm.entity.Trainer;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractDaoTest {

    private TestDao dao;

    @BeforeEach
    void setUp() {
        var storage = new InMemoryStorage();
        dao = new TestDao(storage);
    }

    @Test
    void shouldReturnEmptyWhenEntityDoesNotExist() {
        Optional<Trainer> result = dao.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSaveAndFindById() {
        Trainer trainer = Trainer.builder().build();

        dao.save(1L, trainer);

        Optional<Trainer> result = dao.findById(1L);

        assertThat(result)
                .isPresent()
                .containsSame(trainer);
    }

    @Test
    void shouldReturnAllSavedEntities() {
        Trainer t1 = Trainer.builder().build();
        Trainer t2 = Trainer.builder().build();

        dao.save(1L, t1);
        dao.save(2L, t2);

        List<Trainer> result = dao.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(t1, t2);
    }

    @Test
    void shouldOverwriteEntityOnUpdate() {
        Trainer original = Trainer.builder().build();
        Trainer updated = Trainer.builder().build();

        dao.save(1L, original);
        dao.update(1L, updated);

        Optional<Trainer> result = dao.findById(1L);

        assertThat(result)
                .isPresent()
                .containsSame(updated);
    }

    @Test
    void shouldDeleteEntity() {
        Trainer trainer = Trainer.builder().build();

        dao.save(1L, trainer);
        dao.delete(1L);

        Optional<Trainer> result = dao.findById(1L);

        assertThat(result).isEmpty();
    }
}
