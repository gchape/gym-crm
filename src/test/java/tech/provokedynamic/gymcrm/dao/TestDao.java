package tech.provokedynamic.gymcrm.dao;

import tech.provokedynamic.gymcrm.entity.Entity;
import tech.provokedynamic.gymcrm.entity.Trainer;
import tech.provokedynamic.gymcrm.storage.Storage;

class TestDao extends AbstractDao<Trainer> {
    protected TestDao(Storage<Entity> storage) {
        super(storage, "test");
    }
}
