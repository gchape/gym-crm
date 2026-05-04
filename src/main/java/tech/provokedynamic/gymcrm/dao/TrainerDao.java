package tech.provokedynamic.gymcrm.dao;

import org.springframework.stereotype.Repository;
import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.storage.Storage;

@Repository
public class TrainerDao extends AbstractDao<Trainer> {

    protected TrainerDao(Storage<Entity> storage) {
        super(storage, "trainer");
    }
}
