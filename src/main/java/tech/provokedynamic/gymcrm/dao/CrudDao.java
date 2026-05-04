package tech.provokedynamic.gymcrm.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<T, ID> {
    T save(ID id, T entity);

    T update(ID id, T entity);

    void delete(ID id);

    Optional<T> findById(ID id);

    List<T> findAll();
}
