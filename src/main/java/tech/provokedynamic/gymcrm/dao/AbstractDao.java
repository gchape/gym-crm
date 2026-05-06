package tech.provokedynamic.gymcrm.dao;

import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.storage.Storage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class AbstractDao<T extends Entity> {
    private final String namespace;
    private final Storage<Entity> storage;

    protected AbstractDao(Storage<Entity> storage, String namespace) {
        this.storage = storage;
        this.namespace = namespace;
    }

    public Optional<T> findById(Long id) {
        @SuppressWarnings("unchecked")
        T entity = (T) storage.get(namespace, id);

        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    public List<T> findAll() {
        @SuppressWarnings("unchecked")
        var values = (Collection<T>) storage.getNamespace(namespace).values();

        return values.stream().toList();
    }

    public T save(Long id, T entity) {
        storage.put(namespace, id, entity);

        return entity;
    }

    public T update(Long id, T entity) {
        storage.put(namespace, id, entity);

        return entity;
    }

    public void delete(Long id) {
        storage.delete(namespace, id);
    }
}
