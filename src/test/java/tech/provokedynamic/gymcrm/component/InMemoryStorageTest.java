package tech.provokedynamic.gymcrm.component;

import org.junit.jupiter.api.Test;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.storage.Storage;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryStorageTest {

    Storage<Entity> storage = new InMemoryStorage();

    @Test
    void shouldGenerateCorrectKey() {
        String namespace = "trainer";
        long id = 1L;

        String expected = "trainer:1";
        String actual = Storage.toKeyFn.apply(namespace, id);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldStoreAndRetrieveEntityByKey() {
        String namespace = "trainer";
        long id = 1L;
        Entity entity = Trainer.builder().build();

        storage.put(namespace, id, entity);

        Entity result = storage.get(namespace, id);

        assertThat(result)
                .isNotNull()
                .isEqualTo(entity);
    }

    @Test
    void shouldReturnNullWhenEntityDoesNotExist() {
        Entity result = storage.get("trainer", 999L);

        assertThat(result).isNull();
    }

    @Test
    void shouldOverwriteExistingEntityWhenSameKeyUsed() {
        String namespace = "trainer";
        long id = 1L;

        Entity first = Trainer.builder().build();
        Entity second = Trainer.builder().build();

        storage.put(namespace, id, first);
        storage.put(namespace, id, second);

        Entity result = storage.get(namespace, id);

        assertThat(result).isEqualTo(second);
    }

    @Test
    void shouldDeleteEntitySuccessfully() {
        String namespace = "trainer";
        long id = 1L;

        Entity entity = Trainer.builder().build();

        storage.put(namespace, id, entity);
        storage.delete(namespace, id);

        Entity result = storage.get(namespace, id);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnOnlyEntitiesFromNamespace() {
        storage.put("trainer", 1L, Trainer.builder().build());
        storage.put("trainer", 2L, Trainer.builder().build());
        storage.put("trainee", 1L, Trainer.builder().build());

        Map<String, Entity> result = storage.getNamespace("trainer");

        assertThat(result)
                .hasSize(2)
                .containsKeys("1", "2");
    }

    @Test
    void shouldNotIncludeOtherNamespacesInResult() {
        storage.put("trainer", 1L, Trainer.builder().build());
        storage.put("trainee", 1L, Trainer.builder().build());

        Map<String, Entity> result = storage.getNamespace("trainer");

        assertThat(result).doesNotContainKey("trainee:1");
    }
}
