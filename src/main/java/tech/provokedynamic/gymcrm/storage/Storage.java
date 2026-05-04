package tech.provokedynamic.gymcrm.component;

import tech.provokedynamic.gymcrm.entity.Entity;

import java.util.Map;
import java.util.Optional;

public interface Storage<K extends CharSequence, V extends Entity> {
    void put(K namespace, long id, V entity);

    Optional<V> get(K namespace, long id);

    void delete(K namespace, long id);

    Map<K, V> getNamespace(K namespace);
}
