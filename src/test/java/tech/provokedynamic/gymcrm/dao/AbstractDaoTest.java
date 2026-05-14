package tech.provokedynamic.gymcrm.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.provokedynamic.gymcrm.component.InMemoryStorage;
import tech.provokedynamic.gymcrm.component.StorageKeyFormatter;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.model.Specialization;
import tech.provokedynamic.gymcrm.storage.KeyFormatter;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        InMemoryStorage.class,
        StorageKeyFormatter.class
})
@TestPropertySource(locations = "classpath:application.properties")
class AbstractDaoTest {

    private static final Trainer TRAINER = Trainer.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .username("john.doe")
            .password("password")
            .isActive(true)
            .specialization(Specialization.CARDIO)
            .build();

    private static final Trainer ANOTHER_TRAINER = Trainer.builder()
            .id(2L)
            .firstName("Jane")
            .lastName("Doe")
            .username("jane.doe")
            .password("password")
            .isActive(true)
            .specialization(Specialization.YOGA)
            .build();

    private TestDao dao;

    @Autowired
    private KeyFormatter keyFormatter;

    @BeforeEach
    void setUp() {
        var storage = new InMemoryStorage(keyFormatter);
        dao = new TestDao(storage);
    }

    @Test
    void shouldReturnEmptyWhenEntityDoesNotExist() {
        Optional<Trainer> result = dao.findById(1L);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSaveAndFindById() {
        dao.save(1L, TRAINER);

        Optional<Trainer> result = dao.findById(1L);

        assertThat(result)
                .isPresent()
                .containsSame(TRAINER);
    }

    @Test
    void shouldReturnAllSavedEntities() {
        dao.save(1L, TRAINER);
        dao.save(2L, ANOTHER_TRAINER);

        List<Trainer> result = dao.findAll();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(TRAINER, ANOTHER_TRAINER);
    }

    @Test
    void shouldOverwriteEntityOnUpdate() {
        dao.save(1L, TRAINER);
        dao.update(1L, ANOTHER_TRAINER);

        Optional<Trainer> result = dao.findById(1L);

        assertThat(result)
                .isPresent()
                .containsSame(ANOTHER_TRAINER);
    }

    @Test
    void shouldDeleteEntity() {
        dao.save(1L, TRAINER);
        dao.delete(1L);

        Optional<Trainer> result = dao.findById(1L);

        assertThat(result).isEmpty();
    }
}
