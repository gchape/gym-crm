package tech.provokedynamic.gymcrm.dao;

import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.storage.Storage;

@Repository
public class TrainerDaoImpl extends AbstractDao<Trainer> {

    protected TrainerDaoImpl(Storage<Entity> storage) {
        super(storage, "trainer");
    }
}
