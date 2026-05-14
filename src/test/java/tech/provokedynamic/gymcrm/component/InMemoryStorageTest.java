package tech.provokedynamic.gymcrm.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.model.Specialization;
import tech.provokedynamic.gymcrm.storage.KeyFormatter;
import tech.provokedynamic.gymcrm.storage.Storage;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        InMemoryStorage.class,
        StorageKeyFormatter.class,
})
@TestPropertySource(locations = "classpath:application.properties")
class InMemoryStorageTest {

    private static final Trainer TRAINER_ONE = Trainer.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .username("john.doe")
            .password("password")
            .isActive(true)
            .specialization(Specialization.CARDIO)
            .build();

    private static final Trainer TRAINER_TWO = Trainer.builder()
            .id(2L)
            .firstName("Jane")
            .lastName("Doe")
            .username("jane.doe")
            .password("password")
            .isActive(true)
            .specialization(Specialization.YOGA)
            .build();

    @Autowired
    private Storage<Entity> storage;

    @Autowired
    private KeyFormatter keyFormatter;

    @BeforeEach
    void setUp() {
        storage.clear();
    }

    @Test
    void shouldGenerateCorrectKey() {
        String actual = keyFormatter.format(Storage.Namespace.TRAINER, 1L);

        assertThat(actual).isEqualTo("trainer:1");
    }

    @Test
    void shouldStoreAndRetrieveEntityByKey() {
        storage.put(Storage.Namespace.TRAINER, 1L, TRAINER_ONE);

        Entity result = storage.get(Storage.Namespace.TRAINER, 1L);

        assertThat(result)
                .isNotNull()
                .isEqualTo(TRAINER_ONE);
    }

    @Test
    void shouldReturnNullWhenEntityDoesNotExist() {
        Entity result = storage.get(Storage.Namespace.TRAINER, 999L);

        assertThat(result).isNull();
    }

    @Test
    void shouldOverwriteExistingEntityWhenSameKeyUsed() {
        storage.put(Storage.Namespace.TRAINER, 1L, TRAINER_ONE);
        storage.put(Storage.Namespace.TRAINER, 1L, TRAINER_TWO);

        Entity result = storage.get(Storage.Namespace.TRAINER, 1L);

        assertThat(result).isEqualTo(TRAINER_TWO);
    }

    @Test
    void shouldDeleteEntitySuccessfully() {
        storage.put(Storage.Namespace.TRAINER, 1L, TRAINER_ONE);
        storage.delete(Storage.Namespace.TRAINER, 1L);

        Entity result = storage.get(Storage.Namespace.TRAINER, 1L);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnOnlyEntitiesFromNamespace() {
        storage.put(Storage.Namespace.TRAINER, 1L, TRAINER_ONE);
        storage.put(Storage.Namespace.TRAINER, 2L, TRAINER_TWO);

        Map<String, Entity> result = storage.getNamespace(Storage.Namespace.TRAINER);

        assertThat(result)
                .hasSize(2)
                .containsKeys("1", "2");
    }

    @Test
    void shouldNotIncludeOtherNamespacesInResult() {
        storage.put(Storage.Namespace.TRAINER, 1L, TRAINER_ONE);

        Map<String, Entity> result = storage.getNamespace(Storage.Namespace.TRAINER);

        assertThat(result).doesNotContainKey("trainee:1");
    }
}
