package tech.provokedynamic.gymcrm.storage;

import tech.provokedynamic.gymcrm.entity.Entity;

import java.util.Map;
import java.util.function.BiFunction;

public interface Storage<T extends Entity> {
    String KEY_FORMAT = "%s:%d";
    BiFunction<String, Long, String> toKeyFn = KEY_FORMAT::formatted;

    void put(String namespace, long id, T entity);

    T get(String namespace, long id);

    void delete(String namespace, long id);

    Map<String, T> getNamespace(String namespace);
}
