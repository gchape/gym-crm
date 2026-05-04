package tech.provokedynamic.gymcrm.dao;

import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Training;
import tech.provokedynamic.gymcrm.storage.Storage;

@Repository
public class TrainingDaoImpl extends AbstractDao<Training> {

    protected TrainingDaoImpl(Storage<Entity> storage) {
        super(storage, "training");
    }
}
