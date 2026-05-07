package tech.provokedynamic.gymcrm.component;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.model.Specialization;
import tech.provokedynamic.gymcrm.storage.Storage;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryStorageTest {

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

    Storage<Entity> storage = new InMemoryStorage();

    @Test
    void shouldGenerateCorrectKey() {
        String actual = Storage.toKeyFn.apply("trainer", 1L);

        assertThat(actual).isEqualTo("trainer:1");
    }

    @Test
    void shouldStoreAndRetrieveEntityByKey() {
        storage.put("trainer", 1L, TRAINER);

        Entity result = storage.get("trainer", 1L);

        assertThat(result)
                .isNotNull()
                .isEqualTo(TRAINER);
    }

    @Test
    void shouldReturnNullWhenEntityDoesNotExist() {
        Entity result = storage.get("trainer", 999L);

        assertThat(result).isNull();
    }

    @Test
    void shouldOverwriteExistingEntityWhenSameKeyUsed() {
        storage.put("trainer", 1L, TRAINER);
        storage.put("trainer", 1L, ANOTHER_TRAINER);

        Entity result = storage.get("trainer", 1L);

        assertThat(result).isEqualTo(ANOTHER_TRAINER);
    }

    @Test
    void shouldDeleteEntitySuccessfully() {
        storage.put("trainer", 1L, TRAINER);
        storage.delete("trainer", 1L);

        Entity result = storage.get("trainer", 1L);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnOnlyEntitiesFromNamespace() {
        storage.put("trainer", 1L, TRAINER);
        storage.put("trainer", 2L, ANOTHER_TRAINER);
        storage.put("trainee", 1L, TRAINER);

        Map<String, Entity> result = storage.getNamespace("trainer");

        assertThat(result)
                .hasSize(2)
                .containsKeys("1", "2");
    }

    @Test
    void shouldNotIncludeOtherNamespacesInResult() {
        storage.put("trainer", 1L, TRAINER);
        storage.put("trainee", 1L, ANOTHER_TRAINER);

        Map<String, Entity> result = storage.getNamespace("trainer");

        assertThat(result).doesNotContainKey("trainee:1");
    }
}
